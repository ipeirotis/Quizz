package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.QuizPerformance;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class UserQuizStatisticsServiceTest extends QuizBaseTest {
  private UserQuizStatisticsService userQuizStatisticsService;

  @Before
  public void setUp() {
    super.setUp();

    initUserAnswerService();
    initQuizPerformanceService();
    initQuestionService();
    userQuizStatisticsService = new UserQuizStatisticsService(
        userAnswerService, quizPerformanceService, questionService);
  }

  @Test
  public void testUpdateStatistics() {
    userQuizStatisticsService.updateStatistics(QUIZ_ID1, USER_ID1);
    assertNotNull(quizPerformanceService.get(QUIZ_ID1, USER_ID1));

    QuizPerformance quizPerformance = quizPerformanceService.get(QUIZ_ID1, USER_ID1);
    assertEquals((Integer)2, quizPerformance.getCorrectanswers());
    assertEquals((Integer)1, quizPerformance.getIncorrectanswers());
    assertEquals((Integer)3, quizPerformance.getTotalanswers());
    assertEquals((Integer)1, quizPerformance.getTotalCalibrationAnswers());

    // 3 * Helper.getInformationGain(0.66, NUM_CHOICES).
    assertEquals(1.6861, quizPerformance.getFreqInfoGain(), 0.01);

    // 3 * Helper.getBayesianMeanInformationGain(1, 2, NUM_CHOICES).
    assertEquals(1.56, quizPerformance.getBayesInfoGain(), 0.01);

    // 3 * (Helper.getBayesianMeanInformationGain(1, 2, NUM_CHOICES) -
    //      Math.sqrt(Helper.getBayesianVarianceInformationGain(1, 2, NUM_CHOICES))).
    assertEquals(0.3487, quizPerformance.getLcbInfoGain(), 0.01);

    assertEquals((Integer)2, quizPerformance.getTotalUsers());
    assertEquals((Integer)1, quizPerformance.getRankScore());
  }
}
