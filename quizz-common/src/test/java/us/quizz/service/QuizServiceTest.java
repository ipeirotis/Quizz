package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Quiz;
import us.quizz.utils.QuizBaseTest;

import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class QuizServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initQuizService();
  }

  @Test
  public void testDeleteAll() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", QUIZ_ID1);

    assertNotNull(quizService.get(QUIZ_ID1));
    assertEquals(5, questionService.listAll(params).size());
    assertEquals(7, userAnswerService.listAll(params).size());

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
    assertEquals((Integer)5, quiz.getQuestions());
    assertEquals((Integer)7, quiz.getSubmitted());
    assertEquals((Integer)2, quiz.getGold());
    assertEquals((Integer)3, quiz.getTotalUsers());
    assertEquals((Integer)2, quiz.getContributingUsers());
    assertEquals(0.666, quiz.getConversionRate(), 0.01);
    assertEquals((Integer)3, quiz.getCorrectAnswers());
    assertEquals((Integer)6, quiz.getTotalAnswers());
    assertEquals((Integer)4, quiz.getTotalCalibrationAnswers());
    assertEquals((Integer)2, quiz.getTotalCollectionAnswers());
    assertEquals(4, quiz.getCapacity(), 0.01);
    // TODO(panos): Refactor QuizPerformance to separate the "display" variables
    // (e.g. ,"displayCorrectAnswers" by the internal notion 
    // of correctness of calibration answers, collection answers, etc. 
    // (user2: 2/2 + user1: 1/4) /2:
    // For this metric, we use the correct calibration answers
    // divided by the total answers (including collection)
    assertEquals(0.625, quiz.getAvgUserCorrectness(), 0.01);
    // 3 correct answers divided by 6 total answers.
    assertEquals(0.5, quiz.getAvgAnswerCorrectness(), 0.01);
  }
}
