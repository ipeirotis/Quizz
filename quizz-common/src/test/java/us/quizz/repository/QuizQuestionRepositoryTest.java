package us.quizz.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Question;
import us.quizz.service.QuestionService;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class QuizQuestionRepositoryTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initQuestionService();
  }

  @Test
  public void testDuplicateAnsweredClientID() throws Exception {
    Map<String, Set<Question>> results = questionService.getNextQuizQuestions(QUIZ_ID1, 1, USER_ID1);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    // 0 because the unanswered calibration question has the same client id as an answered question.
    assertEquals(0, results.get(QuestionService.CALIBRATION_KEY).size());
    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(1, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testDuplicateUnansweredClientID() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID1, 5, USER_ID2);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    // Only 1 even though there are two unanswered gold questions, which have the same client id.
    assertEquals(1, results.get(QuestionService.CALIBRATION_KEY).size());
    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(3, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testDuplicateCollectionQuestion() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID1, 5, USER_ID1);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    // Collection questions can be asked infinitely even though it had been answered before.
    assertEquals(3, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testNullOrEmptyClientID() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID2, 5, USER_ID1);
    assertEquals(2, results.size());

    // Make sure all the questions are selected even though there are "repeated" empty
    // and null client id.
    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, results.get(QuestionService.CALIBRATION_KEY).size());
    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(3, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testCollectionQuestion() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID2, 5, USER_ID1);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(3, results.get(QuestionService.COLLECTION_KEY).size());
    for (Question question : results.get(QuestionService.COLLECTION_KEY)) {
      assertTrue(question.getHasSilverAnswers());
      assertFalse(question.getHasGoldAnswer());
    }
  }

  @Test
  public void testCalibrationQuestion() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID2, 5, USER_ID1);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, results.get(QuestionService.CALIBRATION_KEY).size());
    Question question = (Question) results.get(QuestionService.CALIBRATION_KEY).toArray()[0];
    assertFalse(question.getHasSilverAnswers());
    assertTrue(question.getHasGoldAnswer());
  }
}
