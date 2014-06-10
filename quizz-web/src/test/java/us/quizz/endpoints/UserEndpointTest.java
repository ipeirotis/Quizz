package us.quizz.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;

import nl.bitwalker.useragentutils.Browser;

import us.quizz.entities.UserReferal;
import us.quizz.service.UserService;
import us.quizz.utils.QuizWebBaseTest;

import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;

@RunWith(JUnit4.class)
public class UserEndpointTest extends QuizWebBaseTest {
  UserEndpoint userEndpoint;

  @Before
  public void setUp() {
    super.setUp();
    initUserService();
    initUserReferralService();
    userEndpoint = new UserEndpoint(getUserService(), getUserReferralService());
  }

  @Test
  public void testGetUser() {
    assertEquals(4, userReferralService.listAll().size());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("175.0.0.0");
    String userAgentString =
        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/30.0.1599.66 Safari/537.36";
    request.addHeader("User-Agent", userAgentString);

    Map<String, Object> results = userEndpoint.getUser(
        request, "www.google.com/some_ads", QUIZ_ID1);
    assertEquals(1, results.size());
    assertTrue(results.containsKey("userid"));

    String userid = (String) results.get("userid");
    assertNotEquals(USER_ID1, userid);
    assertNotEquals(USER_ID2, userid);
    assertNotEquals(USER_ID3, userid);

    userReferralService.flush();
    assertEquals(5, userReferralService.listAll().size());
    List<UserReferal> userReferals = userReferralService.getUserQuizReferal(userid, QUIZ_ID1);
    assertEquals(1, userReferals.size());
    assertEquals(QUIZ_ID1, userReferals.get(0).getQuiz());
    assertEquals("175.0XXXX", userReferals.get(0).getIpaddress());
    assertEquals(new Text("www.google.com/some_ads"), userReferals.get(0).getReferer());
    assertEquals(Browser.parseUserAgentString(userAgentString), userReferals.get(0).getBrowser());
  }

  @Test
  public void testGetUserExisting() {
    assertEquals(4, userReferralService.listAll().size());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("175.0.0.0");
    String userAgentString =
        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/30.0.1599.66 Safari/537.36";
    request.addHeader("User-Agent", userAgentString);
    Cookie[] cookies = new Cookie[1];
    Cookie cookie = new Cookie(UserService.COOKIE_NAME, USER_ID1);
    cookies[0] = cookie;
    request.setCookies(cookies);

    Map<String, Object> results = userEndpoint.getUser(
        request, "www.google.com/some_ads", QUIZ_ID1);
    assertEquals(1, results.size());
    assertTrue(results.containsKey("userid"));

    String userid = (String) results.get("userid");
    assertEquals(USER_ID1, userid);

    userReferralService.flush();
    // Still +1 user referral even though (USER_ID, QUIZ_ID) already exists since
    // this is a new referral.
    assertEquals(5, userReferralService.listAll().size());

    List<UserReferal> userReferals = userReferralService.getUserQuizReferal(USER_ID1, QUIZ_ID1);
    assertEquals(2, userReferals.size());
  }
}
