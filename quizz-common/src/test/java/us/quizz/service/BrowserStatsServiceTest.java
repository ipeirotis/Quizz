package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.BrowserStats;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class BrowserStatsServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initBrowserStatsService();
  }

  @Test
  public void testUpdateStatistics() {
    assertNull(browserStatsService.get(BROWSER_STRING));
    browserStatsService.updateStatistics(BROWSER_STRING);
    assertNotNull(browserStatsService.get(BROWSER_STRING));

    BrowserStats browserStats = browserStatsService.get(BROWSER_STRING);
    assertEquals(4L, browserStats.getUserCount());
    // 
    assertEquals(8.8, browserStats.getUserScores(), 0.01);
  }
}
