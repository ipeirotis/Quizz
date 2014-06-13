package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.utils.QuizBaseTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class QuizPerformanceTest extends QuizBaseTest {
  private static final String TEST_QUIZ_ID = "test_quiz";
  private static final String TEST_USER_ID = "test_userid";

  @Test
  public void testConstructor() {
    QuizPerformance quiz_performance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    assertEquals(TEST_QUIZ_ID, quiz_performance.getQuiz());
    assertEquals(TEST_USER_ID, quiz_performance.getUserid());
    Integer zero = 0;
    assertEquals(zero, quiz_performance.getTotalanswers());
    assertEquals(zero, quiz_performance.getTotalCalibrationAnswers());
    assertEquals(zero, quiz_performance.getCorrectanswers());
    assertEquals(zero, quiz_performance.getIncorrectanswers());
    assertEquals(0.0, quiz_performance.getScore(), 0.01);

    assertEquals(TEST_USER_ID + "_" + TEST_QUIZ_ID, quiz_performance.getId());
  }

  @Test
  public void testDisplayPercentageCorrect() throws Exception {
    // If the stats is not computed, we default to 0 first. 
    QuizPerformance quizPerformance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    assertEquals("0%", quizPerformance.displayPercentageCorrect());

    quizPerformance.setCorrectanswers(3);
    quizPerformance.setTotalanswers(6);
    assertEquals("50%", quizPerformance.displayPercentageCorrect());
  }

  @Test
  public void testDisplayRankScore() throws Exception {
    QuizPerformance testPerformance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    // Null rank score.
    assertEquals("--", testPerformance.displayRankScore());

    // Total users = 0.
    testPerformance.setTotalUsers(0);
    assertEquals("--", testPerformance.displayRankScore());

    testPerformance.setTotalUsers(5);
    testPerformance.setRankScore(3);
    assertEquals("60%", testPerformance.displayRankScore());
  }
}
