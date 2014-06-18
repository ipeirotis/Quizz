package us.quizz.service;

import com.google.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerAggregationStrategy;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.utils.Helper;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionStatisticsService {
  private static final Logger logger = Logger.getLogger(QuestionStatisticsService.class.getName());
  private static final AnswerAggregationStrategy ANSWER_AGGREGATION_STRATEGY =
      AnswerAggregationStrategy.BAYES_PROB;

  private QuestionService questionService;
  private UserAnswerService userAnswerService;
  private QuizPerformanceService quizPerformanceService;

  @Inject
  public QuestionStatisticsService(
      QuestionService questionService,
      UserAnswerService userAnswerService,
      QuizPerformanceService quizPerformanceService) {
    this.questionService = questionService;
    this.userAnswerService = userAnswerService;
    this.quizPerformanceService = quizPerformanceService;
  }

  // Updates the question statistics of the given questionID.
  public Question updateStatistics(String questionID) {
    Question question = questionService.get(Long.parseLong(questionID));
    if (question == null) {
      throw new IllegalArgumentException("Question with id=" + questionID + " does not exist");
    }

    // Maps from internal answer id -> the information bit for the particular answer.
    Map<Integer, Double> answerBitsMap = new HashMap<Integer, Double>();
    // Maps from internal answer id -> number of UserAnswer for the particular answer.
    Map<Integer, Integer> answerCountsMap = new HashMap<Integer, Integer>();
    // Maps from internal answer id -> list of (user probability, whether user picks this answer) of
    // users giving the answer.
    Map<Integer, List<Pair<Double, Boolean>>> answerProbMap =
        new HashMap<Integer, List<Pair<Double, Boolean>>>();

    resetStatsMap(question, answerBitsMap, answerCountsMap, answerProbMap);
    computeAnswerStatistics(question, answerBitsMap, answerCountsMap, answerProbMap);
    computeQuestionStatistics(question, answerBitsMap, answerCountsMap, answerProbMap);
    computeDifficulty(question);
    questionService.save(question);
    return question;
  }

  // Resets the instance variables of statistics map.
  private void resetStatsMap(Question question,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Pair<Double, Boolean>>> answerProbMap) {
    int numAnswers = question.getAnswers().size();
    for (Answer answer : question.getAnswers()) {
      Integer answerId = answer.getInternalID();
      answerBitsMap.put(answerId, 0.0);
      answerCountsMap.put(answerId, 0);
      answerProbMap.put(answerId, new ArrayList<Pair<Double, Boolean>>());
    }
  }

  // Returns a list of filtered user answers for the given questions, sorted by timestamp.
  // This filters away duplicate answers from the same user (keeping only the first one) and
  // malformed user answer such as having an answer id that is not found in the question. 
  private List<UserAnswer> getSortedSubmittedUserAnswers(Question question) {
    List<UserAnswer> userAnswers =
        userAnswerService.getSubmittedUserAnswersForQuestion(question.getId());
    // Sort UserAnswer result by increasing timestamp. This modifies userAnswers.
    Collections.sort(userAnswers, new Comparator<UserAnswer>() {
      public int compare(UserAnswer userAnswer1, UserAnswer userAnswer2) {
        return (int) (userAnswer1.getTimestamp() - userAnswer2.getTimestamp());
      }
    });

    Set<String> userIds = new HashSet<String>();
    List<UserAnswer> results = new ArrayList<UserAnswer>();
    for (UserAnswer userAnswer : userAnswers) {
      // Skips duplicate answers from the same user, taking only the first answer.
      // TODO(chunhowt): Deal better with duplicate answers.
      if (userIds.contains(userAnswer.getUserid())) {
        continue;
      }
      // Check that the answerID corresponds to a valid answer.
      if (question.getAnswer(userAnswer.getAnswerID()) == null) {
        continue;
      }
      results.add(userAnswer);
      userIds.add(userAnswer.getUserid());
    }
    return results;
  }

  // Updates the instance variables of statistics map for the given question given a user answer's
  // quality and information bit for a given answerId.
  private void updateStatsMap(
      Question question, Double userProb, Double userBits, Integer answerId, Integer numAnswers,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Pair<Double, Boolean>>> answerProbMap) {
    if (!answerBitsMap.containsKey(answerId)) {
      logger.warning("Unrecognized new answerID in answerBitsMap: " + answerId);
      answerBitsMap.put(answerId, 0.0);
    }
    answerBitsMap.put(answerId, answerBitsMap.get(answerId) + userBits);

    if (!answerCountsMap.containsKey(answerId)) {
      logger.warning("Unrecognized new answerID in answerCountsMap: " + answerId);
      answerCountsMap.put(answerId, 0);
    }
    answerCountsMap.put(answerId, answerCountsMap.get(answerId) + 1);

    // Estimate the probability that the answer is correct
    // 
    // NOTE: The computation of correctness below is applicable ONLY
    // for multiple choice questions. For free-text answers, we need
    // to use the Chinese Table process described by Dan Weld.
    // 
    // TODO(ipeirotis): Given that we are using smoothed maximum likelihood estimated for the
    // userProb value, the overall probability estimate is going to be overconfident. Need to
    // check the efficiency of doing some Monte Carlo estimates by sampling repeatedly from
    // the Beta(correct,incorrect) distribution to get the quality of the user, and then
    // estimate a distribution for the ProbCorrect.
    for (Answer answer : question.getAnswers()) {
      List<Pair<Double, Boolean>> currentProb = answerProbMap.get(answer.getInternalID());
      if (currentProb == null) {
        logger.warning("Unrecognized new answerID in answerProbMap: " + answerId);
        currentProb = new ArrayList<Pair<Double, Boolean>>();
      }
      currentProb.add(Pair.of(userProb, answer.getInternalID() == answerId));
      answerProbMap.put(answer.getInternalID(), currentProb);
    }
  }

  // Computes the bayesian posterior probability for each answer of the question based on the list
  // of user quality stored in the global answerProbMap, and stores the result in the Answer
  // entity of the question given.
  // This assumes that each of the user is independent and thus the probability for each answer is
  //   answerProb = PRODUCT(userProb)
  //   where userProb is just the user quality if the answer is the one the user picks and
  //   (1 - user quality) / (numAnswers - 1) if the answer isn't the one the user picks.
  // Refer to BAYES_PROB AnswerAggregationStrategy for more information.
  private void computeBayesProb(Question question,
      Map<Integer, List<Pair<Double, Boolean>>> answerProbMap) {
    double sumProb = 0.0;
    double maxProb = 0.0;
    int bestAnswerID = -1;
    Integer numAnswer = question.getAnswers().size();
    for (Answer answer : question.getAnswers()) {
      Integer internalID = answer.getInternalID();
      List<Pair<Double, Boolean>> probs = answerProbMap.get(internalID);
      Double answerProb = Math.log(1.0 / numAnswer);
      for (Pair<Double, Boolean> prob : probs) {
        if (prob.getRight()) {
          answerProb += Math.log(prob.getLeft());
        } else {
          answerProb += Math.log((1.0 - prob.getLeft()) / (numAnswer - 1));
        }
      }
      answerProb = Math.exp(answerProb);
      answer.setBayesProb(answerProb);
      sumProb += answerProb;
      if (answerProb > maxProb) {
        maxProb = answerProb;
        bestAnswerID = answer.getInternalID();
      }
    }

    // Normalize bayesProb.
    for (Answer answer : question.getAnswers()) {
      answer.setBayesProb(answer.getBayesProb() / sumProb);
    }
    question.setBestBayesProbAnswerID(bestAnswerID);
  }

  // Computes the posterior probability for each answer based on the majority votes, and stores
  // the result in the Answer entitiy of the question given.
  private void computeMajorityVoteProb(Question question,
      Map<Integer, Integer> answerCountsMap) {
    int sumVotes = 0;
    int maxVotes = 0;
    int bestAnswerID = -1;
    for (Answer answer : question.getAnswers()) {
      Integer numVotes = answerCountsMap.get(answer.getInternalID());
      answer.setMajorityVoteProb(numVotes * 1.0);
      sumVotes += numVotes;
      if (numVotes > maxVotes) {
        maxVotes = numVotes;
        bestAnswerID = answer.getInternalID();
      }
    }

    // Normalize majorityVoteProb.
    for (Answer answer : question.getAnswers()) {
      if (sumVotes > 0) {
        answer.setMajorityVoteProb(answer.getMajorityVoteProb() / sumVotes);
      } else {
        answer.setMajorityVoteProb(1.0 / question.getAnswers().size());
      }
    }
    question.setBestMajorityVoteProbAnswerID(bestAnswerID);
  }

  // Computes the posterior probability for each answer based on the weighted votes, and stores
  // the result in the Answer entitiy of the question given. Each vote is weighted by the user
  // quality.
  private void computeWeightedVoteProb(Question question,
      Map<Integer, List<Pair<Double, Boolean>>> answerProbMap) {
    double weightedVotes = 0.0;
    double maxVotes = 0.0;
    int bestAnswerID = -1;
    for (Answer answer : question.getAnswers()) {
      List<Pair<Double, Boolean>> probs = answerProbMap.get(answer.getInternalID());
      double votes = 0.0;
      for (Pair<Double, Boolean> prob : probs) {
        if (prob.getRight()) {
          votes += prob.getLeft();
        }
      }
      answer.setWeightedVoteProb(votes);
      weightedVotes += votes;
      if (votes > maxVotes) {
        maxVotes = votes;
        bestAnswerID = answer.getInternalID();
      }
    }

    // Normalize weightedVoteProb.
    for (Answer answer : question.getAnswers()) {
      if (weightedVotes > 0) {
        answer.setWeightedVoteProb(answer.getWeightedVoteProb() / weightedVotes);
      } else {
        answer.setWeightedVoteProb(1.0 / question.getAnswers().size());
      }
    }
    question.setBestWeightedVoteProbAnswerID(bestAnswerID);
  }

  private void computeBestProbCorrect(Question question) {
    for (Answer answer : question.getAnswers()) {
      switch (ANSWER_AGGREGATION_STRATEGY) {
        case BAYES_PROB:
          answer.setProbCorrect(answer.getBayesProb());
          break;
        case MAJORITY_VOTE:
          answer.setProbCorrect(answer.getMajorityVoteProb());
          break;
        case WEIGHTED_VOTE:
          answer.setProbCorrect(answer.getWeightedVoteProb());
          break;
        default:
          break;
      }
    }
  }

  // Computes the answer statistics for the given question and updates the instance variables of
  // statistics map for each answer and the corresponding fields in the answers in the question
  // passed in.
  // This includes the information bits, number answers and log probability for each answer.
  private void computeAnswerStatistics(Question question,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Pair<Double, Boolean>>> answerProbMap) {
    int numAnswers = question.getAnswers().size();
    List<UserAnswer> userAnswers = getSortedSubmittedUserAnswers(question);
    String quizID = question.getQuizID();
    for (UserAnswer useranswer : userAnswers) {
      String userId = useranswer.getUserid();
      Integer ansId = useranswer.getAnswerID();
      Answer selectedAnswer = question.getAnswer(ansId);
      QuizPerformance qp = quizPerformanceService.get(quizID, userId);
      if (qp == null) {
        continue;
      }

      Double userBits = 0.0, userProb = 0.0;
      Double correct = qp.getCorrectScore();
      if (correct == null) {
        correct = 0d;
      }
      Double total = qp.getTotalScore();
      if (total == null) {
        total = 0d;
      }

      // The probability that the user is correct. We use Laplacian smoothing
      // with 1 being added in the nominator and numAnswers in the denominator.
      userProb = 1.0 * (correct + 1) / (total + numAnswers);

      // Here, we do not count the current question as correct answer & incorrect answer.
      if (question.getKind() == QuestionKind.MULTIPLE_CHOICE_CALIBRATION) {
        if (useranswer.getIsCorrect()) {
          correct--;
        }
        total--;
      } else if (question.getKind() == QuestionKind.MULTIPLE_CHOICE_COLLECTION) {
        // TODO(panos): Discount the current question when it is a collection question.
        // if (userProb > question.getConfidence()) {
        //   total--;
        //   correct -= selectedAnswer.getProbCorrect();
        // }
      }

      // Re-estimate userProb after removing the effect of the current question.
      userProb = 1.0 * (correct + 1) / (total + numAnswers);

      try {
        userBits = Helper.getInformationGain(userProb, numAnswers);
      } catch (Exception e) {
        logger.log(Level.WARNING, "Error when computing bits for user " + userId);
      }
      updateStatsMap(question, userProb, userBits, ansId, numAnswers,
          answerBitsMap, answerCountsMap, answerProbMap);
    }

    // Stores the statistics for each answer.
    for (Answer answer : question.getAnswers()) {
      Integer internalID = answer.getInternalID();
      answer.setBits(answerBitsMap.get(internalID));
      answer.setNumberOfPicks(answerCountsMap.get(internalID));
    }

    computeBayesProb(question, answerProbMap);
    computeMajorityVoteProb(question, answerCountsMap);
    computeWeightedVoteProb(question, answerProbMap);
    computeBestProbCorrect(question);
  }

  // Computes the statistics and best answer for the given question and store them in the given
  // question.
  // Note: This requires the instance variables of statistics map to be updated fully and the
  // probCorrect field of the answer to be populated.
  private void computeQuestionStatistics(Question question,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Pair<Double, Boolean>>> answerProbMap) {
    Double questionBits = 0.0;
    Double sumProb = 0.0;
    Double maxProb = 0.0;
    Boolean isLikelyAnswerCorrect = null;
    Integer likelyAnswerID = 0;
    Integer totalAnswers = 0;
    Integer numCorrect = 0;

    // Loops through the answers to pick the one with the highest probability as the best answer.
    for (Answer answer : question.getAnswers()) {
      Integer answerID = answer.getInternalID();
      Double aProb = answer.getProbCorrect();
      totalAnswers += answerCountsMap.get(answerID);
      if (answer.getKind() == AnswerKind.GOLD) {
        numCorrect = answerCountsMap.get(answerID);
      }

      if (maxProb < aProb) {
        maxProb = aProb;
        likelyAnswerID = answerID;
        if (answer.getKind() == AnswerKind.GOLD) {
          isLikelyAnswerCorrect = true;
        } else if (answer.getKind() == AnswerKind.INCORRECT) {
          isLikelyAnswerCorrect = false;
        } else {
          isLikelyAnswerCorrect = null;
        }
        // TODO(chunhowt): Don't use boolean.
      }
      sumProb += aProb;
      questionBits += answerBitsMap.get(answerID);
    }

    // If it is a collection question, the numCorrect is the one for the best answer.
    if (!question.getHasGoldAnswer()) {
      numCorrect = answerCountsMap.get(likelyAnswerID);
    }
    question.setTotalUserScore(questionBits);
    question.setConfidence(maxProb / sumProb);
    question.setLikelyAnswer(question.getAnswer(likelyAnswerID).getText());
    question.setLikelyAnswerID(likelyAnswerID);
    question.setIsLikelyAnswerCorrect(isLikelyAnswerCorrect);
    question.setHasUserAnswers(totalAnswers > 0);
    question.setNumberOfUserAnswers(totalAnswers);
    question.setNumberOfCorrectUserAnswers(numCorrect);
  }

  // Computes the difficulty of the question given and store it in the question given.
  // Note: This requires the question's numberOfUserAnswers and numberOfCorrectUserAnswers
  // field to be populated.
  private void computeDifficulty(Question question) {
    Integer totalAnswers = question.getNumberOfUserAnswers();
    Integer numCorrect = question.getNumberOfCorrectUserAnswers();
    if (totalAnswers == 0) {
      // If this question has not been answered, its difficulty is the prior.
      question.setDifficulty(question.getDifficultyPrior());
    } else {
      // Else, we can compute the difficulty exactly.
      question.setDifficulty(1.0 - numCorrect / new Double(totalAnswers));
    }
  }
}
