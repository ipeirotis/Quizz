package us.quizz.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;

import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.utils.QuizWebBaseTest;

import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class ProcessUserAnswerEndpointTest extends QuizWebBaseTest {
  ProcessUserAnswerEndpoint processUserAnswerEndpoint;

  @Before
  public void setUp() {
    super.setUp();
    initQuizService();
    initUserService();
    initQuestionService();
    initUserAnswerService();
    initUserAnswerFeedbackService();
    initExplorationExploitationService();
    processUserAnswerEndpoint = new ProcessUserAnswerEndpoint(
        getQuizService(), getUserService(), getQuestionService(), getUserAnswerService(),
        getUserAnswerFeedbackService(), getExplorationExploitationService());
  }

  @Test
  public void testProcessUserAnswer() throws Exception {
    Integer answerID = 0;
    int totalAnswers = 3;
    int correctAnswers = 2;
    int incorrectAnswers = 1;
    int numExploit = 1;

    List<UserAnswer> realUserAnswers = userAnswerService.getUserAnswers(QUIZ_ID1, USER_ID3);
    assertEquals(0, realUserAnswers.size());
    assertNull(userAnswerFeedbackService.get(QUESTION_ID3, USER_ID3));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("175.0.0.0");
    String userAgentString =
        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/30.0.1599.66 Safari/537.36";
    request.addHeader("User-Agent", userAgentString);

    Map<String, Object> result = processUserAnswerEndpoint.processUserAnswer(
        request, QUIZ_ID1, QUESTION_ID3, answerID, USER_ID3,
        correctAnswers, totalAnswers, "", correctAnswers, incorrectAnswers, numExploit);

    assertEquals(3, result.size());
    assertTrue(result.containsKey("userAnswer"));
    assertTrue(result.containsKey("userAnswerFeedback"));
    assertTrue(result.containsKey("exploit"));

    UserAnswer userAnswer = (UserAnswer) result.get("userAnswer");
    assertEquals(userAgentString, userAnswer.getBrowser());
    assertEquals("175.0.0.0", userAnswer.getIpaddress());
    assertEquals(UserAnswer.SUBMIT, userAnswer.getAction());
    assertTrue(userAnswer.getIsCorrect());
    assertEquals(QUIZ_ID1, userAnswer.getQuizID());
    assertEquals("", userAnswer.getUserInput());
    assertEquals(USER_ID3, userAnswer.getUserid());
    assertEquals(QUESTION_ID3, userAnswer.getQuestionID());
    assertEquals(answerID, userAnswer.getAnswerID());

    UserAnswerFeedback userAnswerFeedback = (UserAnswerFeedback) result.get("userAnswerFeedback");
    assertEquals(QUESTION_ID3, userAnswerFeedback.getQuestionID());
    assertEquals(USER_ID3, userAnswerFeedback.getUserid());
    assertEquals(answerID, userAnswerFeedback.getUserAnswerID());
    assertTrue(userAnswerFeedback.getIsCorrect());
    assertEquals((Integer) (correctAnswers + 1), userAnswerFeedback.getNumCorrectAnswers());
    assertEquals((Integer) (totalAnswers + 1), userAnswerFeedback.getNumTotalAnswers());
    assertEquals("Answer 0", userAnswerFeedback.getUserAnswerText());

    // By default, the exploit is false.
    assertFalse((Boolean) result.get("exploit"));

    // For the asyncSave.
    userAnswerFeedbackService.flush();
    userAnswerService.flush();
    realUserAnswers = userAnswerService.getUserAnswers(QUIZ_ID1, USER_ID3);
    assertEquals(1, realUserAnswers.size());
    userAnswer.setId(realUserAnswers.get(0).getId());
    assertEquals(userAnswer, realUserAnswers.get(0));
    assertEquals(userAnswerFeedback, userAnswerFeedbackService.get(QUESTION_ID3, USER_ID3));

    // TODO(chunhowt): Need to test that the update tasks are scheduled properly in the task queues.
  }
}
