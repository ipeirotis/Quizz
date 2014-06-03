package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import us.quizz.entities.User;
import us.quizz.utils.QuizBaseTest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

@RunWith(JUnit4.class)
public class UserServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initUserService();
  }

  @Test
  public void testGetOrCreateUser() {
    // test the get.
    assertEquals(2, userService.listAll().size());
    assertNotNull(userService.getOrCreateUser(USER_ID1));
    assertEquals(2, userService.listAll().size());

    // test the create.
    assertNotNull(userService.getOrCreateUser(USER_ID2));
    assertEquals(3, userService.listAll().size());
  }

  @Test
  public void testGetUserFromCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    // First, get a new cookie if there is no cookie in the request.
    User user = userService.getUserFromCookie(request, response);
    assertNotNull(user);
    assertNotEquals(USER_ID1, user.getUserid());
    assertNotEquals(USER_ID3, user.getUserid());
    assertNotNull(response.getCookie(UserService.COOKIE_NAME));
    Cookie cookie = response.getCookie(UserService.COOKIE_NAME);
    assertEquals(UserService.COOKIE_MAX_AGE, cookie.getMaxAge());
    assertEquals("/", cookie.getPath());

    // Set the cookie in the request and make sure we get the right user back.
    Cookie[] cookies = new Cookie[1];
    cookie = new Cookie(UserService.COOKIE_NAME, USER_ID1);
    cookies[0] = cookie;
    request.setCookies(cookies);
    assertEquals(USER_ID1, userService.getUserFromCookie(request, response).getUserid());
  }
}
