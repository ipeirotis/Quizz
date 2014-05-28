package us.quizz.service;

import com.google.appengine.api.datastore.Text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import nl.bitwalker.useragentutils.Browser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;

import us.quizz.entities.UserReferal;
import us.quizz.utils.QuizBaseTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class UserReferralServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initUserReferralService();
  }

  @Test
  public void testGetUserIDsByQuiz() {
    Set<String> userIds = userReferralService.getUserIDsByQuiz(QUIZ_ID1);
    assertEquals(3, userIds.size());
    assertTrue(userIds.contains(USER_ID1));
    assertTrue(userIds.contains(USER_ID2));
    assertTrue(userIds.contains(USER_ID3));
  }

  @Test
  public void testGetCountByBrowser() {
    Browser browser = Browser.valueOf(BROWSER_STRING);
    UserReferralService.Result result = userReferralService.getCountByBrowser(browser);
    assertEquals(4L, result.getCount());
    assertEquals(4, result.getQuizPerformanceIds().size());
  }

  @Test
  public void testCreateAndStoreUserReferal() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quiz", QUIZ_ID2);
    params.put("userid", USER_ID3);
    List<UserReferal> referals = userReferralService.listAll(params);
    assertEquals(0, referals.size());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("quizID", QUIZ_ID2);
    request.setRemoteAddr("175.0.0.0");
    String userAgentString =
        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/30.0.1599.66 Safari/537.36";
    request.addHeader("User-Agent", userAgentString);
    request.addHeader("Referer", "http://www.google.com/some_ads");

    userReferralService.createAndStoreUserReferal(request, USER_ID3);
    referals = userReferralService.listAll(params);
    assertEquals(1, referals.size());

    UserReferal referal = referals.get(0);
    assertEquals(QUIZ_ID2, referal.getQuiz());
    assertEquals("175.0XXXX", referal.getIpaddress());
    assertEquals(Browser.valueOf(BROWSER_STRING), referal.getBrowser());
    assertEquals(new Text("http://www.google.com/some_ads"), referal.getReferer());
    assertEquals("google.com", referal.getDomain());
  }
}
