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
    initQuizPerformanceService();
  }

  @Test
  public void testGet() {
    QuizPerformance quizPerformance = quizPerformanceService.get(QUIZ_ID1, USER_ID1);
    assertEquals(QUIZ_ID1, quizPerformance.getQuiz());
    assertEquals(USER_ID1, quizPerformance.getUserid());
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
  public void testGetCountsForSurvivalProbability() {
    Map<Integer, Map<Integer, Integer>> results =
        quizPerformanceService.getCountsForSurvivalProbability(QUIZ_ID1);
    assertEquals(4, results.size());
    assertEquals(2, results.get(0).size());
    assertEquals(2, results.get(1).size());
    assertEquals(2, results.get(2).size());
    assertEquals(2, results.get(3).size());
    assertEquals((Integer)2, results.get(0).get(0));
    assertEquals((Integer)2, results.get(1).get(0));
    assertEquals((Integer)1, results.get(2).get(0));
    assertEquals((Integer)1, results.get(3).get(0));
    assertEquals((Integer)1, results.get(0).get(1));
    assertEquals((Integer)1, results.get(1).get(1));
    assertEquals((Integer)1, results.get(2).get(1));
    assertEquals((Integer)1, results.get(3).get(1));
  }

  @Test
  public void testGetScoreSumByIds() {
    Set<String> ids = new HashSet<String>();
    ids.add(QuizPerformance.generateId(QUIZ_ID1, USER_ID1));
    ids.add(QuizPerformance.generateId(QUIZ_ID1, USER_ID2));
    assertEquals(2.12, quizPerformanceService.getScoreSumByIds(ids), 0.01);
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
