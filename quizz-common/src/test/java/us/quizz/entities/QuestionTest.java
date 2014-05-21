package us.quizz.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class QuestionTest extends QuizBaseTest {
  @Test
  public void testConstructor() {
    QuestionKind kind = QuestionKind.MULTIPLE_CHOICE_CALIBRATION;
    String text = "test question";
    String quizID = "test_quiz";
    Question question = new Question(quizID, text, kind);
    assertEquals(quizID, question.getQuizID());
    assertEquals(kind, question.getKind());
    assertEquals(text, question.getQuestionText().getValue());

    assertFalse(question.getHasSilverAnswers());
    assertFalse(question.getHasGoldAnswer());
    assertFalse(question.getHasUserAnswers());

    assertEquals(0, (int) question.getNumberOfUserAnswers());
    assertEquals(0, (int) question.getNumberOfCorrectUserAnswers());
    assertEquals(0, question.getTotalUserScore(), 0.01);
    assertEquals(0, (int) question.getAnswers().size());
  }

  @Test
  public void testGetAnswer() {
    Question question = new Question("test_quiz", "Test question",
        QuestionKind.MULTIPLE_CHOICE_CALIBRATION);
    Answer expected = new Answer(100L, "test_quiz", "Expected answer",
                                 AnswerKind.GOLD, 0);
    question.addAnswer(expected);
    assertEquals(expected, question.getAnswer(0));
    // Index out of bound.
    assertNull(question.getAnswer(1));
  }
}
