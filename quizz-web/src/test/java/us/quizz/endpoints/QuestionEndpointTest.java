package us.quizz.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.utils.QuizWebBaseTest;

@RunWith(JUnit4.class)
public class QuestionEndpointTest extends QuizWebBaseTest {
  QuestionEndpoint questionEndpoint;

  @Before
  public void setUp() {
    super.setUp();
    questionEndpoint = new QuestionEndpoint(getQuizService(), getQuestionService());
    quizService.save(new Quiz("Quiz 1", QUIZ_ID1, QuizKind.MULTIPLE_CHOICE));
  }

  @Test
  public void testInsertQuestion() throws Exception {
    Long questionID = 1256L;
    assertNull(questionService.get(questionID));

    Question question = new Question(
        QUIZ_ID1, new Text("New question"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, questionID,
        "some_client_id", true  /* is Gold */, false  /* Not silver */, 1.2);
    questionEndpoint.insertQuestion(question, authenticatedUser);

    // Make sure questionID is reused if provided.
    assertNotNull(questionService.get(questionID));
  }

  @Test
  public void testInsertQuestionAndAnswers() throws Exception {
    Question question =
        new Question(QUIZ_ID1, new Text("New question"), QuestionKind.MULTIPLE_CHOICE_COLLECTION);
    for (int i = 0; i < 4; ++i) {
      question.addAnswer(new Answer("Answer " + i, AnswerKind.SILVER));
    }
    question = questionEndpoint.insertQuestion(question, authenticatedUser);
    assertTrue(question.getHasSilverAnswers());
    assertFalse(question.getHasGoldAnswer());
    for (int i = 0; i < question.getAnswers().size(); ++i) {
      assertEquals((Integer) i, question.getAnswers().get(i).getInternalID());
      assertEquals(QUIZ_ID1, question.getAnswers().get(i).getQuizID());
    }
  }

  @Test(expected = BadRequestException.class)
  public void testInsertQuestionWrongKindFreeTextCalibration() throws Exception {
    Question question =
        new Question(QUIZ_ID1, new Text("New question"), QuestionKind.FREETEXT_CALIBRATION, 12345L,
                     "some_client_id", true  /* is Gold */, false  /* Not silver */, 1.2);
    questionEndpoint.insertQuestion(question, authenticatedUser);
  }

  @Test(expected = BadRequestException.class)
  public void testInsertQuestionWrongKindFreeTextCollection() throws Exception {
    Question question =
        new Question(QUIZ_ID1, new Text("New question"), QuestionKind.FREETEXT_COLLECTION, 12345L,
                     "some_client_id", false, true, 1.2);
    questionEndpoint.insertQuestion(question, authenticatedUser);
  }

  @Test(expected = BadRequestException.class)
  public void testInsertQuestionWrongKindMultipleChoiceCalibration() throws Exception {
    quizService.save(new Quiz("Quiz 123", "test_quiz", QuizKind.FREE_TEXT));
    Question question =
        new Question("test_quiz", new Text("New question"),
            QuestionKind.MULTIPLE_CHOICE_CALIBRATION, 12345L,
            "some_client_id", true  /* is Gold */, false  /* Not silver */, 1.2);
    questionEndpoint.insertQuestion(question, authenticatedUser);
  }

  @Test(expected = BadRequestException.class)
  public void testInsertQuestionWrongKindMultipleChoiceCollection() throws Exception {
    quizService.save(new Quiz("Quiz 123", "test_quiz", QuizKind.FREE_TEXT));
    Question question =
        new Question("test_quiz", new Text("New question"),
            QuestionKind.MULTIPLE_CHOICE_COLLECTION, 12345L,
            "some_client_id", false, true, 1.2);
    questionEndpoint.insertQuestion(question, authenticatedUser);
  }
}
