package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import nl.bitwalker.useragentutils.Browser;

import us.quizz.entities.BrowserStats;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserReferal;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class BrowserStatsServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    assertNotNull(getBrowserStatsService());

    assertNotNull(getUserReferralService());
    Browser browser = Browser.valueOf(BROWSER_STRING);
    UserReferal userReferal = new UserReferal(USER_ID1);
    userReferal.setQuiz(QUIZ_ID1);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID2);
    userReferal.setQuiz(QUIZ_ID2);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID1);
    userReferal.setQuiz(QUIZ_ID2);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    assertNotNull(getQuizPerformanceService());
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID1);
    quizPerformance.setScore(4.0);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID2, USER_ID2);
    quizPerformance.setScore(2.5);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID3, USER_ID1);
    quizPerformance.setScore(1.6);
    quizPerformanceService.save(quizPerformance);
  }

  @Test
  public void testUpdateStatistics() {
    assertNull(browserStatsService.get(BROWSER_STRING));
    browserStatsService.updateStatistics(BROWSER_STRING);
    assertNotNull(browserStatsService.get(BROWSER_STRING));

    BrowserStats browserStats = browserStatsService.get(BROWSER_STRING);

    // We have three unique UserReferals, even though they belong to only two users -
    // USER_ID1 and USER_ID2, they still count as three.
    assertEquals(3L, browserStats.getUserCount());

    // We have two scores that are relevant here, the rest are not.
    // - (QUIZ_ID1, USER_ID1).
    // - (QUIZ_ID2, USER_ID2).
    assertEquals(6.5, browserStats.getUserScores(), 0.01);
  }
}
