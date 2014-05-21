package us.quizz.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SurvivalProbabilityResultTest {
  @Test
  public void testConstructor() {
    SurvivalProbabilityResult result = new SurvivalProbabilityResult(
        2, 3, 4, 12, 13, 14, 15, 16, 0.3, false);
    assertEquals((Integer)2, result.getCorrectFrom());
    assertEquals((Integer)3, result.getIncorrectFrom());
    assertEquals((Integer)4, result.getExploitFrom());
    assertEquals((Integer)12, result.getCorrectTo());
    assertEquals((Integer)13, result.getIncorrectTo());
    assertEquals((Integer)14, result.getExploitTo());
    assertEquals((Integer)15, result.getUsersFrom());
    assertEquals((Integer)16, result.getUsersTo());
    assertEquals(0.3, result.getProbSurvival(), 0.01);
    assertFalse(result.getIsDefault());

    assertEquals("2_3_4_12_13_14", result.getId());
  }

  @Test
  public void testDefaultConstructor() {
    SurvivalProbabilityResult result = SurvivalProbabilityResult.getDefaultResult(
        2, 3, 4, 12, 13, 14);
    assertEquals((Integer)100, result.getUsersFrom());
    assertEquals((Integer)75, result.getUsersTo());
    assertEquals(0.75, result.getProbSurvival(), 0.01);
    assertTrue(result.getIsDefault());
  }
}
