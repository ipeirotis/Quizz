package us.quizz.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.utils.QuizWebBaseTest;

import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class ProcessUserAnswerEndpointTest extends QuizWebBaseTest {
  ProcessUserAnswerEndpoint processUserAnswerEndpoint;

  @Before
  public void setUp() {
    super.setUp();
    processUserAnswerEndpoint = new ProcessUserAnswerEndpoint(
        getQuizService(), getUserService(), getQuestionService(), getUserAnswerService(),
        getUserAnswerFeedbackService(), getExplorationExploitationService(),
        getUserActionService());

    quizService.save(new Quiz("Quiz 1", QUIZ_ID1, QuizKind.MULTIPLE_CHOICE));
    quizService.save(new Quiz("Quiz 2", QUIZ_ID2, QuizKind.FREE_TEXT));

    Question question1 =
        new Question(
            QUIZ_ID1, new Text("test3"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID3,
            QUESTION_CLIENT_ID3, false, true, 0.3);
    addAnswers(question1, QUESTION_ID3, 4, QUIZ_ID1, false);
    questionService.save(question1);

    Question question2 =
        new Question(
            QUIZ_ID2, new Text("test free text"), QuestionKind.FREETEXT_COLLECTION, QUESTION_ID4,
            QUESTION_CLIENT_ID4, false, false, 0.3);
    question2.addAnswer(new Answer(QUESTION_ID4, QUIZ_ID2, "answer1", AnswerKind.GOLD, 0));
    questionService.save(question2);

    userService.save(new User(USER_ID3));
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
        "", correctAnswers, incorrectAnswers, numExploit, 4);

    assertEquals(5, result.size());
    assertTrue(result.containsKey("userAnswer"));
    assertTrue(result.containsKey("userAnswerFeedback"));
    assertTrue(result.containsKey("exploit"));
    assertTrue(result.containsKey("bestAnswer"));
    assertTrue(result.containsKey("question"));

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
    assertEquals((Integer) 4, userAnswer.getQuestionIndex());

    UserAnswerFeedback userAnswerFeedback = (UserAnswerFeedback) result.get("userAnswerFeedback");
    assertEquals(QUESTION_ID3, userAnswerFeedback.getQuestionID());
    assertEquals(USER_ID3, userAnswerFeedback.getUserid());
    assertEquals(answerID, userAnswerFeedback.getUserAnswerID());
    assertTrue(userAnswerFeedback.getIsCorrect());
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

  @Test
  public void testProcessFreeTextUserAnswerCreateVerificationQuiz() throws Exception {
    String verificationQuizId = QUIZ_ID2 + "-verification";

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("175.0.0.0");
    String userAgentString =
        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/30.0.1599.66 Safari/537.36";
    request.addHeader("User-Agent", userAgentString);

    processUserAnswerEndpoint.processUserAnswer(
        request, QUIZ_ID2, QUESTION_ID4, -1, USER_ID3,
        "answer2", 0, 0, 0, 5);

    Quiz verificationQuiz = quizService.get(verificationQuizId);
    // Ensure that verification quiz is not created.
    assertNull(verificationQuiz);

    Question question = questionService.get(QUESTION_ID4);
    assertEquals(question.getAnswers().size(), 2);

    processUserAnswerEndpoint.processUserAnswer(
        request, QUIZ_ID2, QUESTION_ID4, -1, USER_ID3,
        "answer2", 0, 0, 0, 7);

    // Ensure that verification quiz is created.
    verificationQuiz = quizService.get(verificationQuizId);
    assertEquals(verificationQuiz.getQuizID(), verificationQuizId);
    assertEquals(verificationQuiz.getName(), "Quiz 2 (Verification)");
    assertEquals(verificationQuiz.getKind(), QuizKind.MULTIPLE_CHOICE);
  }
}
