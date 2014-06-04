package us.quizz.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.UserAnswer;
import us.quizz.utils.QuizWebBaseTest;

@RunWith(JUnit4.class)
public class UserAnswerEndpointTest extends QuizWebBaseTest {
  UserAnswerEndpoint userAnswerEndpoint;

  @Before
  public void setUp() {
    super.setUp();
    initUserAnswerService();
    initUserService();
    initAnswerChallengeCounterService();

    userAnswerEndpoint = new UserAnswerEndpoint(
        getUserAnswerService(), getUserService(), getAnswerChallengeCounterService());
  }

  @Test
  public void testAddAnswerFeedback() {
    assertNotNull(answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9));
    assertEquals((Long) 0L, answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9).getCount());

    Long userAnswerID = 6L;
    UserAnswer userAnswer = userAnswerEndpoint.addAnswerFeedback(
        QUIZ_ID2, QUESTION_ID9, userAnswerID, USER_ID1, "new answer feedback");

    assertEquals(new Text("new answer feedback"), userAnswer.getAnswerChallengeText());

    assertNotNull(answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9));
    assertEquals((Long) 1L, answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID9).getCount());
  }
}
