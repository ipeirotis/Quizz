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
    assertEquals(5, userAnswerService.listAll(params).size());

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
    assertEquals((Integer)5, quiz.getSubmitted());
    assertEquals((Integer)2, quiz.getGold());
    assertEquals((Integer)3, quiz.getTotalUsers());
    assertEquals((Integer)2, quiz.getContributingUsers());
    assertEquals(0.666, quiz.getConversionRate(), 0.01);
    assertEquals((Integer)4, quiz.getCorrectAnswers());
    assertEquals((Integer)5, quiz.getTotalAnswers());
    assertEquals((Integer)3, quiz.getTotalCalibrationAnswers());
    assertEquals((Integer)2, quiz.getTotalCollectionAnswers());
    assertEquals(1.06, quiz.getCapacity(), 0.01);
    assertEquals(0.875, quiz.getAvgUserCorrectness(), 0.01);
    assertEquals(0.8, quiz.getAvgAnswerCorrectness(), 0.01);
  }
}
