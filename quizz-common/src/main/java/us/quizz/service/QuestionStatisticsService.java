package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
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

  private QuestionService questionService;
  private UserAnswerService userAnswerService;
  private QuizPerformanceService quizPerformanceService;

  // Maps from internal answer id -> the information bit for the particular answer.
  private Map<Integer, Double> answerBitsMap = new HashMap<Integer, Double>();
  // Maps from internal answer id -> number of UserAnswer for the particular answer.
  private Map<Integer, Integer> answerCountsMap = new HashMap<Integer, Integer>();
  // Maps from internal answer id -> log probability correct of the answer.
  private Map<Integer, Double> answerLogProbMap = new HashMap<Integer, Double>();

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

    resetStatsMap(question);
    computeAnswerStatistics(question);
    computeQuestionStatistics(question);
    computeDifficulty(question);
    questionService.save(question);
    return question;
  }

  // Resets the instance variables of statistics map.
  private void resetStatsMap(Question question) {
    answerBitsMap.clear();
    answerCountsMap.clear();
    answerLogProbMap.clear();

    int numAnswers = question.getAnswers().size();
    for (Answer answer : question.getAnswers()) {
      Integer answerId = answer.getInternalID();
      answerBitsMap.put(answerId, 0.0);
      answerCountsMap.put(answerId, 0);
      // Initially, all answers are equally likely.
      answerLogProbMap.put(answerId, Math.log(1.0 / numAnswers));
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
      Question question, Double userProb, Double userBits, Integer answerId, Integer numAnswers) {
    if (!answerBitsMap.containsKey(answerId)) {
      answerBitsMap.put(answerId, 0.0);
    }
    answerBitsMap.put(answerId, answerBitsMap.get(answerId) + userBits);

    if (!answerCountsMap.containsKey(answerId)) {
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
      Double currentLogProb = answerLogProbMap.get(answer.getInternalID());
      if (answer.getInternalID() == answerId) {
        // If this is the answer chosen by user, multiply it by the new userProb.
        answerLogProbMap.put(answer.getInternalID(), currentLogProb + Math.log(userProb));
      } else {
        // Else, multiply it by assuming that the reverse user (1 - user quality)
        // picks randomly from the other answers. 
        answerLogProbMap.put(
            answer.getInternalID(), currentLogProb + Math.log(((1 - userProb) / (numAnswers - 1))));
      }     
    } 
  }

  // Computes the answer statistics for the given question and updates the instance variables of
  // statistics map for each answer and the corresponding fields in the answers in the question
  // passed in.
  // This includes the information bits, number answers and log probability for each answer.
  private void computeAnswerStatistics(Question question) {
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
      updateStatsMap(question, userProb, userBits, ansId, numAnswers);
    }

    Double sumProb = 0.0;
    for (Double prob : answerLogProbMap.values()) {
      sumProb += Math.exp(prob);
    }
    // Stores the statistics for each answer.
    for (Answer answer : question.getAnswers()) {
      answer.setBits(answerBitsMap.get(answer.getInternalID()));
      answer.setNumberOfPicks(answerCountsMap.get(answer.getInternalID()));
      Double aProbCorrect = Math.exp(answerLogProbMap.get(answer.getInternalID())) / sumProb;
      answer.setProbCorrect(aProbCorrect);
    }
  }

  // Computes the statistics and best answer for the given question and store them in the given
  // question.
  // Note: This requires the instance variables of statistics map to be updated fully.
  private void computeQuestionStatistics(Question question) {
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
      Double aProb = Math.exp(answerLogProbMap.get(answerID));
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
