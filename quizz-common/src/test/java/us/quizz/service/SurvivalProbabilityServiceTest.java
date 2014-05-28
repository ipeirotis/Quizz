package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.utils.QuizBaseTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class SurvivalProbabilityServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initSurvivalProbabilityService();
  }

  @Test
  public void testGetSurvivalProbability() {
    // Both users survive at this stage.
    SurvivalProbabilityResult result = survivalProbabilityService.getSurvivalProbability(
        QUIZ_ID1, 0, 1, 0, 0, 0, 0);
    assertEquals((Integer)2, result.getUsersFrom());
    assertEquals((Integer)2, result.getUsersTo());
    assertEquals(1.0, result.getProbSurvival(), 0.01);

    // USER_1 continues at this stage.
    result = survivalProbabilityService.getSurvivalProbability(
        QUIZ_ID1, 2, 2, 0, 1, 0, 0);
    assertEquals((Integer)1, result.getUsersFrom());
    assertEquals((Integer)1, result.getUsersTo());
    assertEquals(1.0, result.getProbSurvival(), 0.01);

    // USER_1 continues but USER_2 does not.
    result = survivalProbabilityService.getSurvivalProbability(
        QUIZ_ID1, 1, 2, 0, 0, 0, 0);
    assertEquals((Integer)2, result.getUsersFrom());
    assertEquals((Integer)1, result.getUsersTo());
    assertEquals(0.5, result.getProbSurvival(), 0.01);

    // Default survival probability.
    result = survivalProbabilityService.getSurvivalProbability(
        QUIZ_ID1, 2, 2, 1, 2, 0, 0);
    assertEquals((Integer)100, result.getUsersFrom());
    assertEquals((Integer)75, result.getUsersTo());
    assertEquals(0.75, result.getProbSurvival(), 0.01);
  }

  @Test
  public void testGetSurvivalProbabilities() {
    List<SurvivalProbabilityResult> results =
        survivalProbabilityService.getSurvivalProbabilities(QUIZ_ID1);
    assertEquals(10, results.size());
    Map<String, SurvivalProbabilityResult> resultIds =
        new HashMap<String, SurvivalProbabilityResult>();
    for (SurvivalProbabilityResult result : results) {
      resultIds.put(result.getId(), result);
    }
    assertTrue(resultIds.containsKey("0_0_0_0_1_0"));
    assertEquals(0.5, resultIds.get("0_0_0_0_1_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("0_0_0_1_0_0"));
    assertEquals(1.0, resultIds.get("0_0_0_1_0_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("0_1_0_1_1_0"));
    assertEquals(1.0, resultIds.get("0_1_0_1_1_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("1_0_0_1_1_0"));
    assertEquals(0.5, resultIds.get("1_0_0_1_1_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("1_0_0_2_0_0"));
    assertEquals(0.5, resultIds.get("1_0_0_2_0_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("1_1_0_2_1_0"));
    assertEquals(1.0, resultIds.get("1_1_0_2_1_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("2_0_0_2_1_0"));
    assertEquals(1.0, resultIds.get("2_0_0_2_1_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("2_0_0_3_0_0"));
    assertEquals(1.0, resultIds.get("2_0_0_3_0_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("2_1_0_3_1_0"));
    assertEquals(1.0, resultIds.get("2_1_0_3_1_0").getProbSurvival(), 0.01);
    assertTrue(resultIds.containsKey("3_0_0_3_1_0"));
    assertEquals(1.0, resultIds.get("3_0_0_3_1_0").getProbSurvival(), 0.01);
  }

  @Test
  public void testResultsToMap() {
    List<SurvivalProbabilityResult> results =
        survivalProbabilityService.getSurvivalProbabilities(QUIZ_ID1);
    Map<Integer, Map<Integer, Integer>> map = survivalProbabilityService.resultsToMap(results);
    assertEquals(4, map.size());
    assertEquals(2, map.get(0).size());
    assertEquals(2, map.get(1).size());
    assertEquals(2, map.get(2).size());
    assertEquals(2, map.get(3).size());

    assertEquals((Integer)2, map.get(0).get(0));
    assertEquals((Integer)1, map.get(0).get(1));
    assertEquals((Integer)2, map.get(1).get(0));
    assertEquals((Integer)1, map.get(1).get(1));
    assertEquals((Integer)1, map.get(2).get(0));
    assertEquals((Integer)1, map.get(2).get(1));
    assertEquals((Integer)1, map.get(3).get(0));
    assertEquals((Integer)1, map.get(3).get(1));
  }
}
