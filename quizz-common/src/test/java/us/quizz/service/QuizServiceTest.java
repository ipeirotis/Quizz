package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.appengine.api.datastore.Text;

import nl.bitwalker.useragentutils.Browser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserReferal;
import us.quizz.enums.AnswerAggregationStrategy;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.utils.QuizBaseTest;

import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class QuizServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initQuizServiceTest();
  }

  private void initQuizServiceTest() {
    assertNotNull(getQuizService());
    quizService.save(new Quiz("Quiz 1", QUIZ_ID1, QuizKind.MULTIPLE_CHOICE));

    assertNotNull(getQuestionService());
    Question question =
        new Question(
            QUIZ_ID1, new Text("test1"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID1,
            QUESTION_CLIENT_ID1, true  /* is Gold */, false  /* Not silver */, 1.5);
    addAnswers(question, QUESTION_ID1, 4, QUIZ_ID1, true);
    //question.setBestMajorityVoteProbAnswerID(3);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.MAJORITY_VOTE, 3);
    //question.setBestWeightedVoteProbAnswerID(0);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.WEIGHTED_VOTE, 0);
    //question.setBestBayesProbAnswerID(1);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.NAIVE_BAYES, 1);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID1, new Text("test2"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID2,
            QUESTION_CLIENT_ID2, false, true, 0.9);
    addAnswers(question, QUESTION_ID2, 4, QUIZ_ID1, false);
    //question.setBestMajorityVoteProbAnswerID(0);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.MAJORITY_VOTE, 0);
    //question.setBestWeightedVoteProbAnswerID(0);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.WEIGHTED_VOTE, 0);
    //question.setBestBayesProbAnswerID(0);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.NAIVE_BAYES, 0);
    
    
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID1, new Text("test3"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID3,
            QUESTION_CLIENT_ID3, true, false, 0.7);
    addAnswers(question, QUESTION_ID3, 4, QUIZ_ID1, true);
    //question.setBestMajorityVoteProbAnswerID(0);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.MAJORITY_VOTE, 0);
    // question.setBestWeightedVoteProbAnswerID(0);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.WEIGHTED_VOTE, 0);
    // Makes sure that no exception is thrown if the answerID is negative.
    //question.setBestBayesProbAnswerID(-1);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.NAIVE_BAYES, -1);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID1, new Text("test4"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID4,
            QUESTION_CLIENT_ID4, true, false, 1.2);
    addAnswers(question, QUESTION_ID4, 4, QUIZ_ID1, true);
    // Makes sure that no exception is thrown if the answerID is null.
    //question.setBestMajorityVoteProbAnswerID(null);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.MAJORITY_VOTE, null);
    //question.setBestWeightedVoteProbAnswerID(0);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.WEIGHTED_VOTE, 0);
    // Makes sure that no exception is thrown if the answerID is out of bound.
    //question.setBestBayesProbAnswerID(80);
    question.setLikelyAnswerIDForStrategy(AnswerAggregationStrategy.NAIVE_BAYES, 80);
    questionService.save(question);

    assertNotNull(getQuizPerformanceService());
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID1);
    quizPerformance.setScore(1.0);
    quizPerformance.setCorrectanswers(1);
    quizPerformance.setIncorrectanswers(1);
    quizPerformance.setTotalanswers(2);
    quizPerformance.setTotalCalibrationAnswers(1);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID2);
    quizPerformance.setScore(2.0);
    quizPerformance.setCorrectanswers(1);
    quizPerformance.setIncorrectanswers(0);
    quizPerformance.setTotalanswers(1);
    quizPerformance.setTotalCalibrationAnswers(1);
    quizPerformanceService.save(quizPerformance);

    assertNotNull(getUserAnswerService());
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID2, ANSWER_ID0, QUIZ_ID1, true, 1L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID1, ANSWER_ID1, QUIZ_ID1, false, 2L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID2, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true, 3L, UserAnswer.SUBMIT));

    assertNotNull(getUserReferralService());
    Browser browser = Browser.valueOf(BROWSER_STRING);
    UserReferal userReferal = new UserReferal(USER_ID1);
    userReferal.setQuiz(QUIZ_ID1);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID1);
    userReferal.setQuiz(QUIZ_ID2);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID2);
    userReferal.setQuiz(QUIZ_ID1);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID3);
    userReferal.setQuiz(QUIZ_ID1);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);
  }

  @Test
  public void testDeleteAll() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", QUIZ_ID1);

    assertNotNull(quizService.get(QUIZ_ID1));
    assertEquals(4, questionService.listAll(params).size());
    assertEquals(3, userAnswerService.listAll(params).size());

    quizService.deleteRecursively(QUIZ_ID1);
    quizService.flush();

    assertNull(quizService.get(QUIZ_ID1));
    assertEquals(0, questionService.listAll(params).size());
    assertEquals(0, userAnswerService.listAll(params).size());
  }

  @Test
  public void testUpdateQuizCounts() {
    quizService.updateQuizCounts(QUIZ_ID1);
    Quiz quiz = quizService.get(QUIZ_ID1);
    assertEquals((Integer)4, quiz.getQuestions());
    assertEquals((Integer)3, quiz.getSubmitted());
    assertEquals((Integer)3, quiz.getGold());
    assertEquals((Integer)3, quiz.getTotalUsers());
    assertEquals((Integer)2, quiz.getContributingUsers());
    assertEquals(0.666, quiz.getConversionRate(), 0.01);
    assertEquals((Integer)2, quiz.getCorrectAnswers());
    assertEquals((Integer)3, quiz.getTotalAnswers());
    assertEquals((Integer)2, quiz.getTotalCalibrationAnswers());
    assertEquals((Integer)1, quiz.getTotalCollectionAnswers());

    // sum_user (num_bits) / num_users.
    // (2.0 + 1.0) / 2.
    assertEquals(1.5, quiz.getCapacity(), 0.01);

    // For each user, the correctness is the percentage correct, which is the
    // numCorrect / totalAnswer.
    // User1: 1/2.
    // User2: 1/1.
    assertEquals(0.75, quiz.getAvgUserCorrectness(), 0.01);

    // 2 correct answers divided by 3 total answers.
    assertEquals(0.667, quiz.getAvgAnswerCorrectness(), 0.01);

    // Note that QUESTION_ID2 is a collection question, so it is not considered during
    // calculation here.
    // By BayesProb:
    //   QUESTION_ID1 has the wrong best answer.
    //   QUESTION_ID3 has invalid best answer ID (-1), counts as wrong.
    //   QUESTION_ID4 has invalid best answer ID (80), counts as wrong.
    assertEquals(0, quiz.getBayesProbQuizQuality(), 0);

    // By WeightedVoteProb:
    //   QUESTION_ID1, QUESTION_ID3 and QUESTION_ID4 have the right best answer.
    assertEquals(1, quiz.getWeightedVoteProbQuizQuality(), 0);

    // By MajorityVoteProb:
    //   QUESTION_ID1 has the wrong best answer.
    //   QUESTION_ID3 has the right best answer.
    //   QUESTION_ID4 has a null best answer, which counts as wrong.
    assertEquals(1.0 / 3, quiz.getMajorityVoteProbQuizQuality(), 0.01);
  }
}
