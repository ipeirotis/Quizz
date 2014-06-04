package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.KeyFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.enums.AnswerKind;

@RunWith(JUnit4.class)
public class AnswerTest {
  @Test
  public void testConstructor() {
    Long questionID = 123L;
    String quizID = "test_quiz";
    String text = "Test";
    AnswerKind kind = AnswerKind.GOLD;
    Integer internalID = 3;
    Answer answer = new Answer(questionID, quizID, text, kind, internalID);

    assertEquals(questionID, answer.getQuestionID());
    assertEquals(quizID, answer.getQuizID());
    assertEquals(text, answer.getText());
    assertEquals(kind, answer.getKind());
    assertEquals(internalID, answer.getInternalID());
  }
}
