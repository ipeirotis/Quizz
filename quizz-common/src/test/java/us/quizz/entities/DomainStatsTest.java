package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DomainStatsTest {
  @Test
  public void testConstructor() {
    long userCount = 12;
    double userScores = 1.5;
    String domain = "www.nytimes.com";
    DomainStats domainStats = new DomainStats(domain, userCount, userScores);

    assertEquals(userCount, domainStats.getUserCount());
    assertEquals(userScores, domainStats.getUserScores(), 0.01);
    assertEquals(domain, domainStats.getDomain());
  }
} 
