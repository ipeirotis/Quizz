package us.quizz.service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.inject.Inject;

import org.apache.commons.math3.special.Gamma;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionStatisticsService {
  private QuizQuestionRepository quizQuestionRepository;
  private UserAnswerRepository userAnswerRepository;
  private QuizPerformanceRepository quizPerformanceRepository;

  @Inject
  public QuestionStatisticsService(QuizQuestionRepository quizQuestionRepository,
      UserAnswerRepository userAnswerRepository,
      QuizPerformanceRepository quizPerformanceRepository) {
    this.quizQuestionRepository = quizQuestionRepository;
    this.userAnswerRepository = userAnswerRepository;
    this.quizPerformanceRepository = quizPerformanceRepository;
  }

  public Question updateStatistics(String questionID) {
    Question question = quizQuestionRepository.getQuizQuestion(questionID);
    if (question == null) {
      throw new IllegalArgumentException("Question with id=" + questionID + " does not exist");
    }
    int u = getNumberOfUserAnswers(questionID);
    question.setHasUserAnswers((u > 0));
    question.setNumberOfUserAnswers(u);

    int c = getNumberOfCorrectUserAnswers(questionID);
    question.setNumberOfCorrentUserAnswers(c);

    updateAnswerStatistics(question);
    quizQuestionRepository.singleMakePersistent(question, true);
    return question;
  }

  private int getNumberOfUserAnswers(String questionID) {
    return quizQuestionRepository.getNumberOfUserAnswersExcludingIDK(questionID);
  }

  private int getNumberOfCorrectUserAnswers(String questionID) {
    return quizQuestionRepository.getNumberOfCorrectUserAnswers(questionID);
  }

  public void updateAnswerStatistics(Question question) {
    String quizID = question.getQuizID();
    Long questionId = question.getID();

    Map<Integer, Double> answerBits = new HashMap<Integer, Double>();
    Map<Integer, Integer> answerCounts = new HashMap<Integer, Integer>();
    Map<Integer, Double> answerLogit = new HashMap<Integer, Double>();
    for (Answer a : question.getAnswers()) {
      Integer aid = a.getInternalID();
      answerBits.put(aid, 0.0);
      answerCounts.put(aid, 0);

      int n = question.getAnswers().size();
      Double priorlogit = Gamma.digamma(1) - Gamma.digamma(n - 1);
      answerLogit.put(aid, priorlogit);
    }

    List<UserAnswer> userAnswers = userAnswerRepository.getUsersForQuestion(questionId);
    // TODO: We need to check about duplicate answers for the same user for the same question
    for (UserAnswer useranswer : userAnswers) {
      String userId = useranswer.getUserid();
      Integer ansId = useranswer.getAnswerID();

      // Check that the ansId corresponds to an answer
      boolean exists = false;
      for (Answer a : question.getAnswers()) {
        if (ansId == a.getInternalID()) exists = true; 
      }
      if (!exists) continue;
      Double userBits = 0.0;
      Double userLogit = 0.0;

      QuizPerformance qp = quizPerformanceRepository.getQuizPerformance(quizID, userId);
      if (qp != null) {
        userBits = qp.getScore();
        Integer correct = qp.getCorrectanswers();
        if (correct == null) correct = 0;
        Integer incorrect = qp.getIncorrectanswers();
        if (incorrect == null) incorrect = 0;
        // Adding pseudocounts for Bayesian prior
        // All users start with a uniform prior
        correct++;
        incorrect++;
        userLogit = Gamma.digamma(correct) - Gamma.digamma(incorrect);
      } else {
        continue;
      }

      // Update the bits for the answer
      Double currentBits = answerBits.get(ansId);
      if (currentBits == null) currentBits = 0.0;
      answerBits.put(ansId, currentBits + userBits);

      // Update the counts for the answer
      Integer currentCount = answerCounts.get(ansId);
      if (currentCount == null) currentCount = 0;
      answerCounts.put(ansId, currentCount + 1);

      // Estimate the probability that the answer is correct
      // We use the fact that the logit of the probability of being correct
      // is the sum of the logits of the users that picked the answer
      // minus the logits of the users that picked another answer for the
      // same question
      // (We use a Bayesian version of that, hence the digammas, instead of the
      // logs)
      //
      // NOTE: The computation of correctness below is applicable ONLY
      // for multiple choice questions. For free-text answers, we need
      // to use the Chinese Table process described by Dan Weld.
      //
      // We go through all the answers as we need to add the logit in the 
      // selected answer but also need to subtract the logit from the
      // non-selected answers
      for (Answer a : question.getAnswers()) {
        Double currentLogit = answerLogit.get(a.getInternalID());
        // If this is also the answer chosen by the user
        if (a.getInternalID() == ansId) {
          answerLogit.put(a.getInternalID(), currentLogit + userLogit);
        } else {
          answerLogit.put(a.getInternalID(), currentLogit - userLogit);
        }
      }
    }

    // Since we have processed now all the UserAnswer objects, we now
    // update the statistics of the Answer objects and store them
    Double questionBits = 0.0;
    for (Answer a : question.getAnswers()) {
      Double aBits = answerBits.get(a.getInternalID());
      questionBits += aBits;
      Integer aCount = answerCounts.get(a.getInternalID());
      Double aLogit = answerLogit.get(a.getInternalID());
      Double aProbCorrect = Math.exp(aLogit) / (Math.exp(aLogit) + 1);
      a.setBits(aBits);
      a.setNumberOfPicks(aCount);
      a.setProbCorrect(aProbCorrect);
    }
    question.setTotalUserScore(questionBits);
  }
}
