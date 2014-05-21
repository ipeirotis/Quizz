package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.KeyFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AnswerChallengeCounterTest {
  @Test
  public void testConstructor() {
    Long questionID = 123L;
    String quizID = "test_quiz";
    AnswerChallengeCounter answerChallengeCounter =
        new AnswerChallengeCounter(quizID, questionID);

    assertEquals(questionID, answerChallengeCounter.getQuestionID());
    assertEquals(quizID, answerChallengeCounter.getQuizID());
    assertEquals("test_quiz_123", answerChallengeCounter.getId());
  }
} 
