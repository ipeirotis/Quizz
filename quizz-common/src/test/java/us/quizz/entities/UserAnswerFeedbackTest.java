package us.quizz.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UserAnswerFeedbackTest {
  @Test
  public void testConstructor() {
    String userid = "test_userid";
    Long questionID = 100L;
    Integer answerID = 1;
    UserAnswerFeedback userAnswerFeedback =
        new UserAnswerFeedback(questionID, userid, answerID, true);
    assertEquals(userid, userAnswerFeedback.getUserid());
    assertEquals(questionID, userAnswerFeedback.getQuestionID());
    assertEquals(answerID, userAnswerFeedback.getUserAnswerID());
    assertTrue(userAnswerFeedback.getIsCorrect());

    assertEquals("100_test_userid", userAnswerFeedback.getId());
  }

  @Test
  public void testComputeDifficulty() {
    UserAnswerFeedback userAnswerFeedback = new UserAnswerFeedback(100L, "userid", 1, true);

    // When everything is still null, difficulty should be empty.
    userAnswerFeedback.computeDifficulty();
    assertEquals("--", userAnswerFeedback.getDifficulty());

    userAnswerFeedback.setNumCorrectAnswers(10);
    userAnswerFeedback.setNumTotalAnswers(25);
    userAnswerFeedback.computeDifficulty();
    assertEquals("40%", userAnswerFeedback.getDifficulty());
  }
}
