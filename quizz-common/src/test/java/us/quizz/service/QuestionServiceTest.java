package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Question;
import us.quizz.utils.QuizBaseTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class QuestionServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initQuestionService();
  }

  @Test
  public void testGetQuizQuestions() {
    assertEquals(5, questionService.getQuizQuestions(QUIZ_ID1).size());
    assertEquals(4, questionService.getQuizQuestions(QUIZ_ID2).size());
    assertEquals(0, questionService.getQuizQuestions("fake_quiz").size());
  }

  @Test
  public void testGetQuestionClientIDs() {
    Set<Long> questionIDs = new HashSet<Long>();
    questionIDs.add(QUESTION_ID1);
    questionIDs.add(QUESTION_ID2);
    questionIDs.add(QUESTION_ID3);
    questionIDs.add(QUESTION_ID4);  // same client id as QUESTION_ID1.

    Set<String> clientIDs = questionService.getQuestionClientIDs(questionIDs);
    assertEquals(3, clientIDs.size());
    assertTrue(clientIDs.contains(QUESTION_CLIENT_ID1));
    assertTrue(clientIDs.contains(QUESTION_CLIENT_ID2));
    assertTrue(clientIDs.contains(QUESTION_CLIENT_ID3));
  }

  @Test
  public void testGetQuestionClientIDsForNullOrEmptyClientID() {
    Set<Long> questionIDs = new HashSet<Long>();
    questionIDs.add(QUESTION_ID6);
    questionIDs.add(QUESTION_ID7);
    questionIDs.add(QUESTION_ID8);
    questionIDs.add(QUESTION_ID9);

    Set<String> clientIDs = questionService.getQuestionClientIDs(questionIDs);
    assertEquals(0, clientIDs.size());
  }

  @Test
  public void testNextQuestionsDuplicateAnsweredClientID() throws Exception {
    Map<String, Set<Question>> results = questionService.getNextQuizQuestions(QUIZ_ID1, 1, USER_ID1);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    // 0 because the unanswered calibration question has the same client id as an answered question.
    assertEquals(0, results.get(QuestionService.CALIBRATION_KEY).size());
    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(1, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testNextQuestionsDuplicateUnansweredClientID() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID1, 5, USER_ID3);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    // Only 1 even though there are two unanswered gold questions, which have the same client id.
    assertEquals(1, results.get(QuestionService.CALIBRATION_KEY).size());
    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(3, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testNextQuestionsDuplicateCollectionQuestion() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID1, 5, USER_ID1);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    // Collection questions can be asked infinitely even though it had been answered before.
    assertEquals(3, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testNextQuestionsNullOrEmptyClientID() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID2, 5, USER_ID2);
    assertEquals(2, results.size());

    // Make sure all the questions are selected even though there are "repeated" empty
    // and null client id.
    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, results.get(QuestionService.CALIBRATION_KEY).size());
    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(3, results.get(QuestionService.COLLECTION_KEY).size());
  }

  @Test
  public void testNextQuestionsCollectionQuestion() throws Exception {
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
  public void testNextQuestionsCalibrationQuestion() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID2, 5, USER_ID2);
    assertEquals(2, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, results.get(QuestionService.CALIBRATION_KEY).size());
    Question question = (Question) results.get(QuestionService.CALIBRATION_KEY).toArray()[0];
    assertFalse(question.getHasSilverAnswers());
    assertTrue(question.getHasGoldAnswer());
  }

  @Test
  public void testNextQuestionsSortedByUserScore() throws Exception {
    Map<String, Set<Question>> results =
        questionService.getNextQuizQuestions(QUIZ_ID1, 1, USER_ID3);
    assertEquals(2, results.size());

    // Makes sure the sole question selected has the lowest totalUserScore.
    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, results.get(QuestionService.CALIBRATION_KEY).size());
    Question question = (Question) results.get(QuestionService.CALIBRATION_KEY).toArray()[0];
    assertEquals(QUESTION_ID4, question.getId());

    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(1, results.get(QuestionService.COLLECTION_KEY).size());
    question = (Question) results.get(QuestionService.COLLECTION_KEY).toArray()[0];
    assertEquals(QUESTION_ID3, question.getId());
  }

  @Test
  public void testGetNumberOfQuizQuestions() {
    assertEquals((Integer)5, questionService.getNumberOfQuizQuestions(QUIZ_ID1, false));
    assertEquals((Integer)0, questionService.getNumberOfQuizQuestions("fake_quiz", false));
  }

  @Test
  public void testGetNumberOfGoldQuestions() {
    assertEquals((Integer)2, questionService.getNumberOfGoldQuestions(QUIZ_ID1, false));
    assertEquals((Integer)0, questionService.getNumberOfGoldQuestions("fake_quiz", false));
  }
}
