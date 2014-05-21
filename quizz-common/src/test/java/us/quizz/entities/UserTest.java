package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UserTest {
  @Test
  public void testConstructor() {
    String userid = "test_userid";
    User user = new User(userid);
    assertEquals(userid, user.getUserid());
    assertEquals((Integer)3, user.getChallengeBudget());
  }
}
