package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.utils.Helper;

import java.lang.Math;
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

    int u = userAnswerService.getNumberOfUserAnswersExcludingIDK(Long.parseLong(questionID));
    question.setHasUserAnswers(u > 0);
    question.setNumberOfUserAnswers(u);

    int c = userAnswerService.getNumberOfCorrectUserAnswers(Long.parseLong(questionID));
    question.setNumberOfCorrectUserAnswers(c);

    updateAnswerStatistics(question);
    questionService.save(question);
    return question;
  }

  // Updates the answer statistics of the given question.
  // TODO(chunhowt): Refactor this function to make it smaller and unit test it better.
  private void updateAnswerStatistics(Question question) {
    String quizID = question.getQuizID();
    Long questionId = question.getId();

    // STEP 1: set up the default bits, count and probability for each answer id.
    // AnswerId -> Bits.
    Map<Integer, Double> answerBits = new HashMap<Integer, Double>();
    // AnswerId -> UserAnswer count.
    Map<Integer, Integer> answerCounts = new HashMap<Integer, Integer>();
    // AnswerId -> Log Probability correct of the answer.
    Map<Integer, Double> answerLogProb = new HashMap<Integer, Double>();
    int numAnswers = question.getAnswers().size();
    for (Answer a : question.getAnswers()) {
      Integer answerId = a.getInternalID();
      answerBits.put(answerId, 0.0);
      answerCounts.put(answerId, 0);
      // In the beginning, we default to uniform probability for each answer.
      answerLogProb.put(answerId, Math.log(1.0 / numAnswers));
    }

    // STEP 2: loop through all the user answers to aggregate the bits, counts and probability for
    // each answer id.
    List<UserAnswer> userAnswers = userAnswerService.getSubmittedUserAnswersForQuestion(questionId);
    // Sort UserAnswer result by increasing timestamp. This modifies userAnswers.
    Collections.sort(userAnswers, new Comparator<UserAnswer>() {
      public int compare(UserAnswer userAnswer1, UserAnswer userAnswer2) {
        return (int) (userAnswer1.getTimestamp() - userAnswer2.getTimestamp());
      }
    });
    Set<String> userIds = new HashSet<String>();
    for (UserAnswer useranswer : userAnswers) {
      String userId = useranswer.getUserid();
      if (userIds.contains(userId)) {
        continue;  // Skip duplicate answers from the same user, taking only the first answer.
      }
      userIds.add(userId);
      Integer ansId = useranswer.getAnswerID();

      // Check that the ansId corresponds to an answer
      Answer selectedAnswer = null;
      for (Answer a : question.getAnswers()) {
        if (ansId == a.getInternalID()) {
          selectedAnswer = a;
          break;
        }
      }
      if (selectedAnswer == null) {
        continue;
      }

      Double userBits = 0.0;
      Double userProb;
      QuizPerformance qp = quizPerformanceService.get(quizID, userId);
      if (qp != null) {
        Double correct = qp.getCorrectScore();
        if (correct == null) {
          correct = 0d;
        }
        Double total = qp.getTotalScore();
        if (total == null) {
          total = 0d;
        }
        
        // The probability that the user is correct. We use Laplacean smoothing
        // with 1 being added in the nominator and N in the denominator
        userProb = 1.0 * (correct + 1) / (total + numAnswers);

        // Here, we do not count the current question as correct answer & incorrect answer.
        if (question.getKind()==QuestionKind.MULTIPLE_CHOICE_CALIBRATION) {
          if (useranswer.getIsCorrect()) {
            correct--;
          }
          total--;
        } else if (question.getKind()==QuestionKind.MULTIPLE_CHOICE_COLLECTION) {
          /*
          if (userProb > question.getConfidence()) {
            total--;
            correct -= selectedAnswer.getProbCorrect();
          }
          */
        }

        // Re-estimate userProb
        userProb = 1.0 * (correct + 1) / (total + numAnswers);
        try {
          userBits = Helper.getInformationGain(userProb, numAnswers);
        } catch (Exception e) {
          logger.log(Level.WARNING, "Error when computing bits for user " + userId);
        }
      } else {
        continue;
      }

      // Update the bits for the answer
      Double currentBits = answerBits.get(ansId);
      if (currentBits == null) {
        currentBits = 0.0;
      }
      answerBits.put(ansId, currentBits + userBits);

      // Update the counts for the answer
      Integer currentCount = answerCounts.get(ansId);
      if (currentCount == null) {
        currentCount = 0;
      }
      answerCounts.put(ansId, currentCount + 1);

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
      // TODO(chunhowt): Figure out the computation of answerLogProb here.
      for (Answer a : question.getAnswers()) {
        Double currentLogProb = answerLogProb.get(a.getInternalID());
        // If this is also the answer chosen by the user
        if (a.getInternalID() == ansId) {
          answerLogProb.put(a.getInternalID(), currentLogProb + Math.log(userProb));
        } else {
          answerLogProb.put(a.getInternalID(),
                            currentLogProb + Math.log(((1 - userProb) / (numAnswers - 1))));
        }
      }
    }

    // STEP 3: Determines the best answer and stores the statistics for question and each answer.
    Double questionBits = 0.0;
    Double sumProb = 0.0;
    Double maxProb = 0.0;
    String likelyAnswer = "";
    Boolean isLikelyAnswerCorrect = null;
    for (Answer a : question.getAnswers()) {
      Double aProb = Math.exp(answerLogProb.get(a.getInternalID()));
      if (maxProb < aProb) {
        maxProb = aProb;
        likelyAnswer = a.getText();
        if (a.getKind() == AnswerKind.GOLD) {
          isLikelyAnswerCorrect = true;
        } else if (a.getKind() == AnswerKind.INCORRECT) {
          isLikelyAnswerCorrect = false;
        } else {
          isLikelyAnswerCorrect = null;
        }
        // TODO(chunhowt): Don't use boolean.
      }
      sumProb += aProb;
      questionBits += answerBits.get(a.getInternalID());
    }
    question.setTotalUserScore(questionBits);
    question.setConfidence(maxProb / sumProb);
    question.setLikelyAnswer(likelyAnswer);
    question.setIsLikelyAnswerCorrect(isLikelyAnswerCorrect);

    for (Answer a : question.getAnswers()) {
      Double aBits = answerBits.get(a.getInternalID());
      a.setBits(aBits);
      Integer aCount = answerCounts.get(a.getInternalID());
      a.setNumberOfPicks(aCount);
      Double aProbCorrect = Math.exp(answerLogProb.get(a.getInternalID())) / sumProb;
      a.setProbCorrect(aProbCorrect);
    }
  }
}
