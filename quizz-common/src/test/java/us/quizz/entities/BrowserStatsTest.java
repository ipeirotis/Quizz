package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import nl.bitwalker.useragentutils.Browser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BrowserStatsTest {
  @Test
  public void testConstructor() {
    long userCount = 12;
    double userScores = 1.5;
    String userAgentString =
        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/30.0.1599.66 Safari/537.36";
    Browser browser = Browser.parseUserAgentString(userAgentString);
    BrowserStats browserStats = new BrowserStats(browser, userCount, userScores);

    assertEquals(userCount, browserStats.getUserCount());
    assertEquals(userScores, browserStats.getUserScores(), 0.01);
    assertEquals("CHROME", browserStats.getId());
    assertEquals(browser, browserStats.getBrowser());
    assertEquals("Chrome", browserStats.getBrowserName());
  }
} 
