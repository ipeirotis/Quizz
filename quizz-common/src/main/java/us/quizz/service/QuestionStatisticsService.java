package us.quizz.service;

import com.google.inject.Inject;

import org.apache.commons.lang3.tuple.Triple;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerAggregationStrategy;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.utils.Helper;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionStatisticsService {
  private static final Logger logger = Logger.getLogger(QuestionStatisticsService.class.getName());
  private static final String ANSWER_AGGREGATION_STRATEGY =
      AnswerAggregationStrategy.NAIVE_BAYES.toString();

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
    // Maps from internal answer id -> list of (user correct answers, user incorrect answers,
    // whether user picks this answer) of users giving the answer.
    Map<Integer, List<Triple<Double, Double, Boolean>>> answerProbMap =
        new HashMap<Integer, List<Triple<Double, Double, Boolean>>>();

    resetStatsMap(question, answerBitsMap, answerCountsMap, answerProbMap);
    computeAnswerStatistics(question, answerBitsMap, answerCountsMap, answerProbMap);
    computeQuestionStatistics(question, answerBitsMap, answerCountsMap, answerProbMap);
    computeDifficulty(question);
    computeEntropyOfQuestionAnswers(question);
    questionService.save(question);
    return question;
  }

  // Resets the instance variables of statistics map.
  private void resetStatsMap(Question question,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Triple<Double, Double, Boolean>>> answerProbMap) {
    for (Answer answer : question.getAnswers()) {
      Integer answerId = answer.getInternalID();
      answerBitsMap.put(answerId, 0.0);
      answerCountsMap.put(answerId, 0);
      answerProbMap.put(answerId, new ArrayList<Triple<Double, Double, Boolean>>());
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
      Question question, Double userCorrect, Double userIncorrect, Double userBits,
      Integer answerId, Integer numAnswers,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Triple<Double, Double, Boolean>>> answerProbMap) {
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

    for (Answer answer : question.getAnswers()) {
      List<Triple<Double, Double, Boolean>> currentProb = answerProbMap.get(answer.getInternalID());
      if (currentProb == null) {
        logger.warning("Unrecognized new answerID in answerProbMap: " + answerId);
        currentProb = new ArrayList<Triple<Double, Double, Boolean>>();
      }
      currentProb.add(Triple.of(userCorrect, userIncorrect, answer.getInternalID() == answerId));
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
      Map<Integer, List<Triple<Double, Double, Boolean>>> answerProbMap) {
    AnswerAggregationStrategy strategy = AnswerAggregationStrategy.NAIVE_BAYES;
    double sumProb = 0.0;
    double maxProb = 0.0;
    int bestAnswerID = -1;
    Integer numAnswer = question.getAnswers().size();
    for (Answer answer : question.getAnswers()) {
      Integer internalID = answer.getInternalID();
      List<Triple<Double, Double, Boolean>> probs = answerProbMap.get(internalID);
      Double answerProb = Math.log(1.0 / numAnswer);
      for (Triple<Double, Double, Boolean> prob : probs) {
        Double correct = prob.getLeft();
        Double incorrect = prob.getMiddle();
        Double smoothedProbability = (correct + 1) / (correct + incorrect + numAnswer);
        Boolean answeredCorrectly = prob.getRight();
        
        if (answeredCorrectly) {
          answerProb += Math.log(smoothedProbability);
        } else {
          answerProb += Math.log((1.0 - smoothedProbability) / (numAnswer - 1));
        }
      }
      answerProb = Math.exp(answerProb);
      answer.setProbCorrectForStrategy(strategy, answerProb);
      sumProb += answerProb;
      if (answerProb > maxProb) {
        maxProb = answerProb;
        bestAnswerID = answer.getInternalID();
      }
    }

    // Normalize bayesProb.
    for (Answer answer : question.getAnswers()) {
      Double v = answer.getProbCorrectForStrategy(strategy) / sumProb;
      answer.setProbCorrectForStrategy(strategy, v);;
    }
    question.setLikelyAnswerIDForStrategy(strategy, bestAnswerID);
  }

  // Computes the posterior probability for each answer based on the majority votes, and stores
  // the result in the Answer entitiy of the question given.
  private void computeMajorityVoteProb(Question question,
      Map<Integer, Integer> answerCountsMap) {
    AnswerAggregationStrategy strategy = AnswerAggregationStrategy.MAJORITY_VOTE;
    int sumVotes = 0;
    int maxVotes = 0;
    int bestAnswerID = -1;
    for (Answer answer : question.getAnswers()) {
      Integer numVotes = answerCountsMap.get(answer.getInternalID());
      answer.setProbCorrectForStrategy(strategy, numVotes * 1.0);
      sumVotes += numVotes;
      if (numVotes > maxVotes) {
        maxVotes = numVotes;
        bestAnswerID = answer.getInternalID();
      }
    }

    // Normalize majorityVoteProb.
    for (Answer answer : question.getAnswers()) {
      if (sumVotes > 0) {
        Double v = answer.getProbCorrectForStrategy(strategy) / sumVotes;
        answer.setProbCorrectForStrategy(strategy, v);
      } else {
        Integer numAnswer = question.getAnswers().size();
        Double v = 1.0 / numAnswer;
        answer.setProbCorrectForStrategy(strategy, v);
      }
    }
    question.setLikelyAnswerIDForStrategy(strategy, bestAnswerID);
  }

  // Computes the posterior probability for each answer based on the weighted votes, and stores
  // the result in the Answer entitiy of the question given. Each vote is weighted by the user
  // quality.
  private void computeWeightedVoteProb(Question question,
      Map<Integer, List<Triple<Double, Double, Boolean>>> answerProbMap) {
    AnswerAggregationStrategy strategy = AnswerAggregationStrategy.WEIGHTED_VOTE;
    double weightedVotes = 0.0;
    double maxVotes = 0.0;
    int bestAnswerID = -1;
    Integer numAnswer = question.getAnswers().size();
    for (Answer answer : question.getAnswers()) {
      List<Triple<Double, Double, Boolean>> probs = answerProbMap.get(answer.getInternalID());
      double votes = 0.0;
      for (Triple<Double, Double, Boolean> prob : probs) {
        Double correct = prob.getLeft();
        Double incorrect = prob.getMiddle();
        Double smoothedProbability = (correct + 1) / (correct + incorrect + numAnswer);
        Boolean answeredCorrectly = prob.getRight();
        if (answeredCorrectly) {
          votes += smoothedProbability;
        }
      }
      answer.setProbCorrectForStrategy(strategy, votes);
      weightedVotes += votes;
      if (votes > maxVotes) {
        maxVotes = votes;
        bestAnswerID = answer.getInternalID();
      }
    }

    // Normalize weightedVoteProb.
    for (Answer answer : question.getAnswers()) {
      if (weightedVotes > 0) {
        Double v = answer.getProbCorrectForStrategy(strategy) / weightedVotes;
        answer.setProbCorrectForStrategy(strategy, v);
      } else {
        Double v =  1.0 / numAnswer;
        answer.setProbCorrectForStrategy(strategy, v);
      }
    }
    question.setLikelyAnswerIDForStrategy(strategy, bestAnswerID);
  }

  // Computes the answer statistics for the given question and updates the instance variables of
  // statistics map for each answer and the corresponding fields in the answers in the question
  // passed in.
  // This includes the information bits, number answers and log probability for each answer.
  private void computeAnswerStatistics(Question question,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Triple<Double, Double, Boolean>>> answerProbMap) {
    int numAnswers = question.getAnswers().size();
    List<UserAnswer> userAnswers = getSortedSubmittedUserAnswers(question);
    String quizID = question.getQuizID();
    for (UserAnswer useranswer : userAnswers) {
      String userId = useranswer.getUserid();
      Integer ansId = useranswer.getAnswerID();
      QuizPerformance qp = quizPerformanceService.get(quizID, userId);
      if (qp == null) {
        continue;
      }

      Double correct = qp.getCorrectScore();
      Double total = qp.getTotalScore();

      // Here, we do not count the current question as correct answer & incorrect answer.
      if (question.getKind() == QuestionKind.MULTIPLE_CHOICE_CALIBRATION) {
        if (useranswer.getIsCorrect()) {
          correct--;
        }
        total--;
      }

      // Estimate userProb after removing the effect of the current question.
      Double userProb = 1.0 * (correct + 1) / (total + numAnswers);
      Double userBits = Helper.getInformationGain(userProb, numAnswers);
      updateStatsMap(question, correct, total - correct, userBits, ansId, numAnswers,
          answerBitsMap, answerCountsMap, answerProbMap);
    }

    // Stores the statistics for each answer.
    for (Answer answer : question.getAnswers()) {
      Integer internalID = answer.getInternalID();
      answer.setBits(answerBitsMap.get(internalID));
      answer.setNumberOfPicks(answerCountsMap.get(internalID));
    }
  }

  // Computes the statistics and best answer for the given question and store them in the given
  // question.
  // Note: This requires the instance variables of statistics map to be updated fully and the
  // probCorrect field of the answer to be populated.
  private void computeQuestionStatistics(Question question,
      Map<Integer, Double> answerBitsMap,
      Map<Integer, Integer> answerCountsMap,
      Map<Integer, List<Triple<Double, Double, Boolean>>> answerProbMap) {
    computeBayesProb(question, answerProbMap);
    computeMajorityVoteProb(question, answerCountsMap);
    computeWeightedVoteProb(question, answerProbMap);

    computeTotalResponses(question, answerCountsMap);
    computeBitsForQuestion(question, answerBitsMap);

    // Loops through the answers to pick the one with the highest probability as the best answer.
    Map<String, Integer> likelyAnswerIDmap = computeLikelyAnswers(question);
    computeNumberOfCorrectResponses(question, answerCountsMap, likelyAnswerIDmap);

    // TODO(panos): The Confidence and IsLikelyAnswerCorrect need to be refactored to have an
    // AnswerAggregationStrategy associated with them.
    Integer answerId = likelyAnswerIDmap.get(ANSWER_AGGREGATION_STRATEGY);
    for (Answer answer : question.getAnswers()) {
      if (answer.getInternalID() == answerId) {
        question.setConfidence(answer.getProbCorrects().get(ANSWER_AGGREGATION_STRATEGY));
        if (answer.getKind() == AnswerKind.GOLD) {
          question.setIsLikelyAnswerCorrect(true);
        } else if  (answer.getKind() == AnswerKind.INCORRECT) {
          question.setIsLikelyAnswerCorrect(false);
        } else {
          question.setIsLikelyAnswerCorrect(null);
        }
      }
    }    
  }

  private void computeTotalResponses(Question question, Map<Integer, Integer> answerCountsMap) {
    Integer totalAnswers = 0;
    for (Answer answer : question.getAnswers()) {
      Integer answerID = answer.getInternalID();
      totalAnswers += answerCountsMap.get(answerID);
    }
    question.setHasUserAnswers(totalAnswers > 0);
    question.setNumberOfUserAnswers(totalAnswers);
  }

  private Map<String, Integer> computeLikelyAnswers(Question question) {
    Map<String, Integer> likelyAnswerIDmap = new HashMap<String, Integer>();
    for (AnswerAggregationStrategy strategy : AnswerAggregationStrategy.values()) {
      Integer likelyAnswerID = 0;
      Double maxProb = 0.0;
      
      for (Answer answer : question.getAnswers()) {
        Integer answerID = answer.getInternalID();
        Double aProb = answer.getProbCorrectForStrategy(strategy);
        if (maxProb < aProb) {
          maxProb = aProb;
          likelyAnswerID = answerID;
        }
      }
      likelyAnswerIDmap.put(strategy.toString(), likelyAnswerID);
    }
    question.setLikelyAnswerIDs(likelyAnswerIDmap);
    
    Map<String, String> likelyAnswerMap = new HashMap<String, String>();
    for (AnswerAggregationStrategy strategy : AnswerAggregationStrategy.values()) {
      Integer answerId = likelyAnswerIDmap.get(strategy.toString());
      likelyAnswerMap.put(strategy.toString(), question.getAnswer(answerId).getText());
    }
    question.setLikelyAnswer(likelyAnswerMap);
    
    return likelyAnswerIDmap;
  }

  private void computeNumberOfCorrectResponses(
      Question question, Map<Integer, Integer> answerCountsMap,
      Map<String, Integer> likelyAnswerIDmap) {
    Integer numCorrect = 0;
    for (Answer answer : question.getAnswers()) {
      Integer answerID = answer.getInternalID();
      if (answer.getKind() == AnswerKind.GOLD) {
        numCorrect = answerCountsMap.get(answerID);
      }
    }
    
    // If it is a collection question, the numCorrect is the one for the best answer.
    if (!question.getHasGoldAnswer()) {
      numCorrect = answerCountsMap.get(likelyAnswerIDmap.get(ANSWER_AGGREGATION_STRATEGY));
    }
    
    question.setNumberOfCorrectUserAnswers(numCorrect);
  }

  private void computeBitsForQuestion(Question question, Map<Integer, Double> answerBitsMap) {
    Double questionBits = 0.0;
    for (Answer answer : question.getAnswers()) {
      Integer answerID = answer.getInternalID();
      questionBits += answerBitsMap.get(answerID);
    }
    question.setTotalUserScore(questionBits);
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

  /**
   * Computes the entropy over the probabilities for the answers of the question,
   * with the probabilities computed using various aggregation strategies
   *
   * @param question
   */
  public Question computeEntropyOfQuestionAnswers(Question question) {
    Map<String, Double> result = new HashMap<String, Double>();
    for (AnswerAggregationStrategy strategy : AnswerAggregationStrategy.values()) {
      double entropy = 0.0;
      for (Answer ans : question.getAnswers()) {
        double p = ans.getProbCorrectForStrategy(strategy);
        if (p > 0) entropy += - p * Math.log(p);
      }
      result.put(strategy.toString(), entropy);
    }
    question.setEntropy(result);
    return question;
  }
}
