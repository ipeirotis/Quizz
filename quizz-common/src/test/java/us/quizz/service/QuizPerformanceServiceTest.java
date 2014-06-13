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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class QuizPerformanceServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initUserAnswerService();
    initQuestionService();
    initQuizPerformanceService();
  }

  @Test
  public void testGet() {
    QuizPerformance quizPerformance = quizPerformanceService.get(QUIZ_ID1, USER_ID1);
    assertEquals(QUIZ_ID1, quizPerformance.getQuiz());
    assertEquals(USER_ID1, quizPerformance.getUserid());
  }

  @Test
  public void testUpdateStatistics() {
    quizPerformanceService.updateStatistics(QUIZ_ID1, USER_ID1);
    assertNotNull(quizPerformanceService.get(QUIZ_ID1, USER_ID1));

    QuizPerformance quizPerformance = quizPerformanceService.get(QUIZ_ID1, USER_ID1);
    assertEquals((Integer)3, quizPerformance.getCorrectanswers());
    assertEquals((Integer)1, quizPerformance.getIncorrectanswers());
    assertEquals((Integer)4, quizPerformance.getTotalanswers());
    assertEquals((Integer)2, quizPerformance.getTotalCalibrationAnswers());

    // 4 * Helper.getInformationGain(2/6, NUM_CHOICES).
    // assertEquals(1.6861, quizPerformance.getFreqInfoGain(), 0.01);

    // 4 * Helper.getBayesianMeanInformationGain(1, 2, NUM_CHOICES).
    // assertEquals(1.56, quizPerformance.getBayesInfoGain(), 0.01);

    // 4 * (Helper.getBayesianMeanInformationGain(1, 2, NUM_CHOICES) -
    //      Math.sqrt(Helper.getBayesianVarianceInformationGain(1, 2, NUM_CHOICES))).
    // assertEquals(0.3487, quizPerformance.getLcbInfoGain(), 0.01);

    assertEquals((Integer)2, quizPerformance.getTotalUsers());
    assertEquals((Integer)2, quizPerformance.getRankScore());
  }
  
  @Test
  public void testDelete() {
    assertNotNull(quizPerformanceService.get(QUIZ_ID1, USER_ID1));
    quizPerformanceService.delete(QUIZ_ID1, USER_ID1);
    quizPerformanceService.flush();
    assertNull(quizPerformanceService.get(QUIZ_ID1, USER_ID1));
  }

  @Test
  public void testGetQuizPerformanceByQuiz() {
    List<QuizPerformance> quizPerformances =
        quizPerformanceService.getQuizPerformancesByQuiz(QUIZ_ID1);
    assertEquals(2, quizPerformances.size());

    // null quizID means get everything.
    quizPerformances = quizPerformanceService.getQuizPerformancesByQuiz(null);
    assertEquals(3, quizPerformances.size());
  }

  @Test
  public void testGetQuizPerformanceByUser() {
    List<QuizPerformance> quizPerformances =
        quizPerformanceService.getQuizPerformancesByUser(USER_ID1);
    assertEquals(2, quizPerformances.size());

    // null userID means get everything.
    quizPerformances = quizPerformanceService.getQuizPerformancesByUser(null);
    assertEquals(3, quizPerformances.size());
  }


  @Test
  public void testGetScoreSumByIds() {
    Set<String> ids = new HashSet<String>();
    ids.add(QuizPerformance.generateId(QUIZ_ID1, USER_ID1));
    ids.add(QuizPerformance.generateId(QUIZ_ID1, USER_ID2));
    assertEquals(8.0, quizPerformanceService.getScoreSumByIds(ids), 0.01);
  }

  @Test
  public void testGetScoreSumByIdsEmpty() {
    Set<String> ids = new HashSet<String>();
    assertEquals(0, quizPerformanceService.getScoreSumByIds(ids), 0.01);
  }

  @Test
  public void testGetScoreSumByIdsInvalid() {
    Set<String> ids = new HashSet<String>();
    ids.add(QuizPerformance.generateId(QUIZ_ID2, USER_ID2));
    assertEquals(0, quizPerformanceService.getScoreSumByIds(ids), 0.01);
  }
}
