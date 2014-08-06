package us.quizz.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.enums.QuizKind;

@RunWith(JUnit4.class)
public class QuizTest {
  @Test
  public void testConstructor() {
    String quiz_name = "Test quiz name";
    String quiz_id = "test_quiz";
    QuizKind quiz_kind = QuizKind.MULTIPLE_CHOICE;
    Quiz quiz = new Quiz(quiz_name, quiz_id, quiz_kind);
    assertEquals(quiz_id, quiz.getQuizID());
    assertEquals(quiz_kind, quiz.getKind());
    assertEquals(quiz_name, quiz.getName());
    assertFalse(quiz.getShowOnDefault());

    assertNull(quiz.getQuestionSelectionStrategy());
  }
}
