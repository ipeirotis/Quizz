package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UserAnswerTest {
  @Test
  public void testConstructor() {
    String userid = "test_userid";
    UserAnswer userAnswer = new UserAnswer(userid, "100", "1");
    assertEquals(userid, userAnswer.getUserid());
    assertEquals((Long)100L, userAnswer.getQuestionID());
    assertEquals((Integer)1, userAnswer.getAnswerID());

    userAnswer = new UserAnswer(userid, 100L, 1);
    assertEquals(userid, userAnswer.getUserid());
    assertEquals((Long)100L, userAnswer.getQuestionID());
    assertEquals((Integer)1, userAnswer.getAnswerID());
  }
}
