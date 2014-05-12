package us.quizz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.utils.Helper;

import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;

public class QuestionStatisticsService {

  private static final Logger logger = Logger.getLogger(QuestionStatisticsService.class.getName());

  private QuizService quizService;
  private QuizQuestionRepository quizQuestionRepository;
  private UserAnswerService userAnswerService;
  private QuizPerformanceRepository quizPerformanceRepository;

  @Inject
  public QuestionStatisticsService(QuizService quizService, QuizQuestionRepository quizQuestionRepository,
      UserAnswerService userAnswerService, QuizPerformanceRepository quizPerformanceRepository) {
    this.quizService = quizService;
    this.quizQuestionRepository = quizQuestionRepository;
    this.userAnswerService = userAnswerService;
    this.quizPerformanceRepository = quizPerformanceRepository;
  }

  public Question updateStatistics(String questionID) {
    Question question = quizQuestionRepository.getQuizQuestion(questionID);
    if (question == null) {
      throw new IllegalArgumentException("Question with id=" + questionID + " does not exist");
    }

    Quiz quiz = quizService.get(question.getQuizID());

    if (question.getKind()==null) {
      Boolean isCalibration = false;
      for (Answer a : question.getAnswers()) {
        if (a.getKind() == AnswerKind.GOLD) {
          isCalibration = true;
          break;
        } else if (a.getKind() == AnswerKind.SILVER) {
          isCalibration = false;
          break;
        }
      }
  
      if (quiz.getKind() == QuizKind.MULTIPLE_CHOICE) {
        if (isCalibration) {
          question.setKind(QuestionKind.MULTIPLE_CHOICE_CALIBRATION);
          logger.log(Level.INFO, "Question:" + question.getID() + " is set to kind MULTIPLE_CHOICE_CALIBRATION");
        } else {
          question.setKind(QuestionKind.MULTIPLE_CHOICE_COLLECTION);
          logger.log(Level.INFO, "Question:" + question.getID() + " is set to kind MULTIPLE_CHOICE_COLLECTION");
        }
      } else if (quiz.getKind() == QuizKind.FREE_TEXT) {
        if (isCalibration) {
          question.setKind(QuestionKind.FREETEXT_CALIBRATION);
          logger.log(Level.INFO, "Question:" + question.getID() + " is set to kind FREETEXT_CALIBRATION");
        } else {
          question.setKind(QuestionKind.FREETEXT_COLLECTION);
          logger.log(Level.INFO, "Question:" + question.getID() + " is set to kind FREETEXT_COLLECTION");
        }
      }
    }
    
    if (question.getQuestionText()==null) {
      question.setQuestionText(new Text(question.getText()));
      
    }

    int u = userAnswerService.getNumberOfUserAnswersExcludingIDK(Long.parseLong(questionID));
    question.setHasUserAnswers((u > 0));
    question.setNumberOfUserAnswers(u);

    int c = userAnswerService.getNumberOfCorrectUserAnswers(Long.parseLong(questionID));
    question.setNumberOfCorrentUserAnswers(c);

    updateAnswerStatistics(question);
    quizQuestionRepository.singleMakePersistent(question, true);
    return question;
  }

  public void updateAnswerStatistics(Question question) {
    String quizID = question.getQuizID();
    Long questionId = question.getID();

    Map<Integer, Double> answerBits = new HashMap<Integer, Double>();
    Map<Integer, Integer> answerCounts = new HashMap<Integer, Integer>();
    Map<Integer, Double> answerProb = new HashMap<Integer, Double>();
    int n = question.getAnswers().size();
    for (Answer a : question.getAnswers()) {
      Integer aid = a.getInternalID();
      answerBits.put(aid, 0.0);
      answerCounts.put(aid, 0);
      answerProb.put(aid, 1.0 / n);
    }

    List<UserAnswer> userAnswers = userAnswerService.getUserAnswersForQuestion(questionId);
    // TODO: We need to check about duplicate answers for the same user for the
    // same question
    for (UserAnswer useranswer : userAnswers) {
      String userId = useranswer.getUserid();
      Integer ansId = useranswer.getAnswerID();

      // Check that the ansId corresponds to an answer
      boolean exists = false;
      Answer selectedAnswer = null;
      for (Answer a : question.getAnswers()) {
        if (ansId == a.getInternalID()) {
          exists = true;
          selectedAnswer = a;
        }
      }
      if (!exists)
        continue;
      Double userBits = 0.0;
      Double userProb;

      QuizPerformance qp = quizPerformanceRepository.getQuizPerformance(quizID, userId);
      if (qp != null) {
        Integer correct = qp.getCorrectanswers();
        if (correct == null)
          correct = 0;
        Integer incorrect = qp.getIncorrectanswers();
        if (incorrect == null)
          incorrect = 0;

        if (selectedAnswer.getKind() == AnswerKind.GOLD) {
          correct--;
        } else if (selectedAnswer.getKind() == AnswerKind.INCORRECT) {
          incorrect--;
        }

        userProb = 1.0 * (correct + 1) / (correct + incorrect + n);
        try {
          userBits = Helper.getInformationGain(userProb, n);
        } catch (Exception e) {
          logger.log(Level.WARNING, "Error when computing bits for user " + userId);
        }
      } else {
        continue;
      }

      // Update the bits for the answer
      Double currentBits = answerBits.get(ansId);
      if (currentBits == null)
        currentBits = 0.0;
      answerBits.put(ansId, currentBits + userBits);

      // Update the counts for the answer
      Integer currentCount = answerCounts.get(ansId);
      if (currentCount == null)
        currentCount = 0;
      answerCounts.put(ansId, currentCount + 1);

      // Estimate the probability that the answer is correct
      //
      // NOTE: The computation of correctness below is applicable ONLY
      // for multiple choice questions. For free-text answers, we need
      // to use the Chinese Table process described by Dan Weld.
      //
      // Given that we are using smoothed maximum likelihood estimated
      // for the userProb value, the overall probability estimate
      // is going to be overconfident. Need to check the efficiency
      // of doing some Monte Carlo estimates by sampling repeatedly
      // from the Beta(correct,incorrect) distribution to get the quality
      // of the user, and then estimate a distribution for the ProbCorrect

      for (Answer a : question.getAnswers()) {
        Double currentProb = answerProb.get(a.getInternalID());
        // If this is also the answer chosen by the user
        if (a.getInternalID() == ansId) {
          answerProb.put(a.getInternalID(), currentProb * userProb);
        } else {
          answerProb.put(a.getInternalID(), currentProb * ((1 - userProb) / (n - 1)));
        }
      }
    }

    // Since we have processed now all the UserAnswer objects, we now
    // update the statistics of the Question and Answer objects and store them
    Double questionBits = 0.0;
    Double sumProb = 0.0;
    Double maxProb = 0.0;
    String likelyAnswer = "";
    Boolean isLikelyAnswerCorrect = null;
    for (Answer a : question.getAnswers()) {
      Double aProb = answerProb.get(a.getInternalID());
      if (maxProb < aProb) {
        maxProb = aProb;
        likelyAnswer = a.getText();
        if (a.getKind() == AnswerKind.GOLD) {
          isLikelyAnswerCorrect = true;
        } else if (a.getKind() == AnswerKind.INCORRECT) {
          isLikelyAnswerCorrect = false;
        }
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
      Double aProbCorrect = answerProb.get(a.getInternalID()) / sumProb;
      a.setProbCorrect(aProbCorrect);
    }

  }
}
