package us.quizz.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.UserAnswer;
import us.quizz.utils.QuizWebBaseTest;

@RunWith(JUnit4.class)
public class UserAnswerEndpointTest extends QuizWebBaseTest {
  UserAnswerEndpoint userAnswerEndpoint;

  @Before
  public void setUp() {
    super.setUp();
    userAnswerEndpoint = new UserAnswerEndpoint(
        getUserAnswerService(), getUserService(), getAnswerChallengeCounterService());
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID2, QUESTION_ID9));
  }

  @Test
  public void testAddAnswerFeedback() {
    assertNotNull(answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9));
    assertEquals((Long) 0L, answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9).getCount());

    UserAnswer userAnswer = userAnswerService.save(
       new UserAnswer(
           USER_ID1, QUESTION_ID9, ANSWER_ID0, QUIZ_ID2, true, 1L, UserAnswer.SUBMIT));
    Long userAnswerID = userAnswer.getId();
    userAnswer = userAnswerEndpoint.addAnswerFeedback(
        QUIZ_ID2, QUESTION_ID9, userAnswerID, USER_ID1, "new answer feedback",
        "correct value", "http://url_evidence", "WRONG_ANSWER");

    assertEquals(new Text("new answer feedback"), userAnswer.getAnswerChallengeText());
    assertEquals(new Text("correct value"), userAnswer.getAnswerChallengeCorrectValue());
    assertEquals(new Text("http://url_evidence"), userAnswer.getAnswerChallengeUrlSupport());
    assertEquals("WRONG_ANSWER", userAnswer.getAnswerChallengeReason());

    assertNotNull(answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9));
    assertEquals((Long) 1L, answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9).getCount());
  }
}
