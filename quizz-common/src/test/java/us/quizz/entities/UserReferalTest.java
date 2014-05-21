package us.quizz.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UserReferalTest {
  @Test
  public void testConstructor() {
    String userid = "test_userid";
    UserReferal userReferal = new UserReferal(userid);
    assertEquals(userid, userReferal.getUserid());
    assertNotNull(userReferal.getTimestamp());
  }

  @Test
  public void testSetIpAddress() {
    UserReferal userReferal = new UserReferal("userid");
    assertNull(userReferal.getIpaddress());

    userReferal.setIpaddress("116.913.11.22");
    assertEquals("116.913.1XXXX", userReferal.getIpaddress());
  }
}
