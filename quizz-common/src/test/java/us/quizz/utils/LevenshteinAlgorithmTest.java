package us.quizz.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LevenshteinAlgorithmTest {
  @Test
  public void testGetLevenshteinDistance() {
    assertEquals(0, LevenshteinAlgorithm.getLevenshteinDistance("", ""));
    assertEquals(2, LevenshteinAlgorithm.getLevenshteinDistance("", "ab"));
    assertEquals(3, LevenshteinAlgorithm.getLevenshteinDistance("cde", ""));
    assertEquals(1, LevenshteinAlgorithm.getLevenshteinDistance("aac", "abc"));
    assertEquals(1, LevenshteinAlgorithm.getLevenshteinDistance("acca", "aca"));
    assertEquals(2, LevenshteinAlgorithm.getLevenshteinDistance("abccba", "ccba"));
    assertEquals(3, LevenshteinAlgorithm.getLevenshteinDistance("abcdabc", "abbbc"));
    assertEquals(6, LevenshteinAlgorithm.getLevenshteinDistance("b10z8a", "az8101"));
  }
}
