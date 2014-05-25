package us.quizz.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class AnswerChallengeCounterServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initAnswerChallengeCounterService();
  }

  @Test
  public void testGet() {
    assertNotNull(answerChallengeCounterService.get(QUIZ_ID1, QUESTION_ID4));
    assertNull(answerChallengeCounterService.get(QUIZ_ID2, QUESTION_ID2));
  }
}
