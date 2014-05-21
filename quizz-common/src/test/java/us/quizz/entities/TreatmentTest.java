package us.quizz.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TreatmentTest {
  @Test
  public void testConstructor() {
    String name = "Show Leaderboard";
    Double probability = 0.4;
    Treatment treatment = new Treatment(name, probability);
    assertEquals(name, treatment.getName());
    assertEquals(probability, treatment.getProbability(), 0.01);

    assertFalse(treatment.getBlocksAll());
    assertNotNull(treatment.getBlocks());
    assertNotNull(treatment.getBlockedBy());
  }
} 

