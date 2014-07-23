package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.utils.QuizBaseTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class QuestionServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initQuestionServiceTest();
  }

  private void initQuestionServiceTest() {
    assertNotNull(getQuizService());

    Quiz quiz1 = new Quiz("quiz1", QUIZ_ID1, QuizKind.MULTIPLE_CHOICE);
    quizService.save(quiz1);

    Quiz quiz2 = new Quiz("quiz2", QUIZ_ID2, QuizKind.MULTIPLE_CHOICE);
    quizService.save(quiz2);

    Quiz quiz3 = new Quiz("quiz3", QUIZ_ID3, QuizKind.FREE_TEXT);
    quizService.save(quiz3);

    assertNotNull(getQuestionService());

    // Quiz 1 has 5 questions, 2 are calibration, 3 are collections.
    // Question 1 and 4 have the same client id.
    Question question =
        new Question(
            QUIZ_ID1, new Text("test1"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID1,
            QUESTION_CLIENT_ID1, true  /* is Gold */, false  /* Not silver */, 1.5);
    addAnswers(question, QUESTION_ID1, 4, QUIZ_ID1, true);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID1, new Text("test2"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID2,
            QUESTION_CLIENT_ID2, false, true, 0.9);
    addAnswers(question, QUESTION_ID2, 4, QUIZ_ID1, false);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID1, new Text("test3"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID3,
            QUESTION_CLIENT_ID3, false, true, 0.3);
    addAnswers(question, QUESTION_ID3, 4, QUIZ_ID1, false);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID1, new Text("test4"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID4,
            QUESTION_CLIENT_ID1, true, false, 1.1);
    addAnswers(question, QUESTION_ID4, 4, QUIZ_ID1, true);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID1, new Text("test5"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID5,
            QUESTION_CLIENT_ID4, false, true, 0.45);
    addAnswers(question, QUESTION_ID5, 4, QUIZ_ID1, false);
    questionService.save(question);

    // Quiz 2 has 4 questions, 1 is calibration, 3 are collections.
    // All the questions have null or empty client id.
    question =
        new Question(
            QUIZ_ID2, new Text("test6"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID6, "",
            true, false, 1.5);
    addAnswers(question, QUESTION_ID6, 4, QUIZ_ID2, true);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID2, new Text("test7"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID7, "",
            false, true, 0.7);
    addAnswers(question, QUESTION_ID7, 4, QUIZ_ID2, false);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID2, new Text("test8"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID8,
            null, false, true, 0.3);
    addAnswers(question, QUESTION_ID8, 4, QUIZ_ID2, false);
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID2, new Text("test9"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID9,
            null, false, true, 0.2);
    addAnswers(question, QUESTION_ID9, 4, QUIZ_ID2, false);
    questionService.save(question);

    // Quiz 3 has 2 free text question
    question =
        new Question(
            QUIZ_ID3, new Text("test10"), QuestionKind.FREETEXT_CALIBRATION, QUESTION_ID10,
            QUESTION_CLIENT_ID1, true, false, 0d);
    question.addAnswer(new Answer(QUESTION_ID10, QUIZ_ID3, "answer", AnswerKind.GOLD, 0));
    question.addAnswer(
        new Answer(QUESTION_ID10, QUIZ_ID3, "user submitted answer", AnswerKind.USER_SUBMITTED, 1));
    questionService.save(question);

    question =
        new Question(
            QUIZ_ID3, new Text("test11"), QuestionKind.FREETEXT_COLLECTION, QUESTION_ID11,
            QUESTION_CLIENT_ID2, false, true, 0d);
    questionService.save(question);

    // User 1 answers 4 questions (2 collection, 2 calibration) from quiz 1,
    // 2 of which are duplicate questions.
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID2, ANSWER_ID0, QUIZ_ID1, true, 1L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID1, ANSWER_ID1, QUIZ_ID1, false, 2L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true, 3L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID3, ANSWER_ID0, QUIZ_ID1, true, 4L, UserAnswer.SUBMIT));

    // User 1 also answers 2 questions from quiz 2.
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID6, ANSWER_ID0, QUIZ_ID2, true, 5L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID9, ANSWER_ID2, QUIZ_ID2, false, 6L, UserAnswer.SUBMIT));

    // User 2 answers 1 calibration question from quiz 1, correctly.
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID2, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true, 7L, UserAnswer.SUBMIT));

    // User 3 answers 0 questions.
    userService.save(new User(USER_ID3));
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
    // All the client ids are null or empty, so they are dropped.
    assertEquals(0, clientIDs.size());
  }

  @Test
  public void testNextQuestionsDuplicateAnsweredClientID() throws Exception {
    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID1, USER_ID1);
    assertEquals(3, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    // There are only two calibration question - QUESTION_ID1 and QUESTION_ID4 in QUIZ_ID1.
    // USER_ID1 has answered QUESTION_ID1, and since QUESTION_ID1 and QUESTION_ID4 has the
    // same client id, it is not repeated.
    assertEquals(0, ((Set<Question>)results.get(QuestionService.CALIBRATION_KEY)).size());
  }

  @Test
  public void testNextQuestionsDuplicateUnansweredClientID() throws Exception {
    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID1, USER_ID3);
    assertEquals(3, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    // Only 1 even though there are two unanswered gold questions, since both of them have the
    // same client id.
    assertEquals(1, ((Set<Question>)results.get(QuestionService.CALIBRATION_KEY)).size());
  }

  @Test
  public void testNextQuestionsDuplicateCollectionQuestion() throws Exception {
    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID1, USER_ID1);
    assertEquals(3, results.size());

    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    // There are 3 collection questions and USER_ID1 has answered QUESTION_ID2 and QUESTION_ID3
    // before.
    // However, collection questions can be asked infinitely even though it had been answered
    // before, so they are all chosen again.
    assertEquals(3, ((Set<Question>)results.get(QuestionService.COLLECTION_KEY)).size());
  }

  @Test
  public void testNextQuestionsNullOrEmptyClientID() throws Exception {
    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID2, USER_ID2);
    assertEquals(3, results.size());

    // Make sure all the questions are selected even though there are "repeated" empty
    // and null client id.
    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, ((Set<Question>)results.get(QuestionService.CALIBRATION_KEY)).size());
    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(3, ((Set<Question>)results.get(QuestionService.COLLECTION_KEY)).size());
  }

  @Test
  public void testNextQuestionsCollectionQuestion() throws Exception {
    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID2, USER_ID1);
    assertEquals(3, results.size());

    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(3, ((Set<Question>)results.get(QuestionService.COLLECTION_KEY)).size());
    for (Question question : (Set<Question>) results.get(QuestionService.COLLECTION_KEY)) {
      assertTrue(question.getHasSilverAnswers());
      assertFalse(question.getHasGoldAnswer());
    }
  }

  @Test
  public void testNextQuestionsCalibrationQuestion() throws Exception {
    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID2, USER_ID2);
    assertEquals(3, results.size());

    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, ((Set<Question>)results.get(QuestionService.CALIBRATION_KEY)).size());
    Question question =
        (Question) ((Set<Question>)results.get(QuestionService.CALIBRATION_KEY)).toArray()[0];
    assertFalse(question.getHasSilverAnswers());
    assertTrue(question.getHasGoldAnswer());
  }

  @Test
  public void testNextQuestionsSortedByUserScore() throws Exception {
    User user = userService.get(USER_ID3);
    user.setNumQuestionsLimit(1);
    userService.save(user);

    Quiz quiz = quizService.get(QUIZ_ID1);
    quiz.setAllowVaryingLengthQuizSession(true);
    quizService.save(quiz);

    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID1, USER_ID3);
    assertEquals(3, results.size());

    // Makes sure the sole question selected has the lowest totalUserScore.
    assertTrue(results.containsKey(QuestionService.CALIBRATION_KEY));
    assertEquals(1, ((Set<Question>)results.get(QuestionService.CALIBRATION_KEY)).size());
    Question question =
        (Question) ((Set<Question>)results.get(QuestionService.CALIBRATION_KEY)).toArray()[0];
    assertEquals(QUESTION_ID4, question.getId());

    assertTrue(results.containsKey(QuestionService.COLLECTION_KEY));
    assertEquals(1, ((Set<Question>)results.get(QuestionService.COLLECTION_KEY)).size());
    question = (Question) ((Set<Question>)results.get(QuestionService.COLLECTION_KEY)).toArray()[0];
    assertEquals(QUESTION_ID3, question.getId());

    assertTrue(results.containsKey(QuestionService.NUM_QUESTIONS_KEY));
    assertEquals((Integer)1, (Integer) results.get(QuestionService.NUM_QUESTIONS_KEY));
  }

  @Test
  public void testNextQuestionsDefaultNumQuestions() throws Exception {
    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID1, USER_ID3);
    assertEquals(3, results.size());
    assertTrue(results.containsKey(QuestionService.NUM_QUESTIONS_KEY));
    assertEquals((Integer) QuestionService.DEFAULT_NUM_QUESTIONS_PER_QUIZ,
                 (Integer) results.get(QuestionService.NUM_QUESTIONS_KEY));
  }

  @Test
  public void testNextQuestionsUserNumQuestions() throws Exception {
    User user = userService.get(USER_ID3);
    user.setNumQuestionsLimit(8);
    userService.save(user);

    Quiz quiz = quizService.get(QUIZ_ID1);
    quiz.setAllowVaryingLengthQuizSession(true);
    quizService.save(quiz);

    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID1, USER_ID3);
    assertEquals(3, results.size());
    assertTrue(results.containsKey(QuestionService.NUM_QUESTIONS_KEY));
    assertEquals((Integer) 8, (Integer) results.get(QuestionService.NUM_QUESTIONS_KEY));
  }

  @Test
  public void testNextQuestionsUnlimitedNumQuestions() throws Exception {
    User user = userService.get(USER_ID3);
    user.setNumQuestionsLimit(-1);
    userService.save(user);

    Quiz quiz = quizService.get(QUIZ_ID1);
    quiz.setAllowVaryingLengthQuizSession(true);
    quizService.save(quiz);

    Map<String, Object> results = questionService.getNextQuizQuestions(QUIZ_ID1, USER_ID3);
    assertEquals(3, results.size());
    assertTrue(results.containsKey(QuestionService.NUM_QUESTIONS_KEY));
    assertEquals(-1, (int) (Integer) results.get(QuestionService.NUM_QUESTIONS_KEY));
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

  @Test
  public void testVerifyAnswerFreeTextCorrect() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID10), -1, "answer");
    assertTrue(result.getIsCorrect());
    assertEquals("Great! The correct answer is indeed answer!", result.getMessage());
  }

  @Test
  public void testVerifyAnswerFreeTextWithTypo() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID10), -1, "answe");
    assertTrue(result.getIsCorrect());
    assertEquals("Nice! Close one! The correct answer is answer!",
                 result.getMessage());
  }

  @Test
  public void testVerifyAnswerFreeTextCalibrationInCorrect() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID10), -1, "wrong answer");
    assertFalse(result.getIsCorrect());
    assertEquals("Sorry! The correct answer is answer", result.getMessage());
  }

  @Test
  public void testVerifyAnswerFreeTextUserAnswer() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID10), -1, "user submitted answer");
    assertTrue(result.getIsCorrect());
    assertEquals(
        "Well done! We don't know the answer for this question, but other users "
            + "submitted the same answer!", result.getMessage());
  }

  @Test
  public void testVerifyAnswerFreeTextUserAnswerWithTypo() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID10), -1, "user submitted answe");
    assertTrue(result.getIsCorrect());
    assertEquals(
        "Well done! We don't know the answer for this question, but other users "
            + "submitted almost the same answer!", result.getMessage());
  }

  @Test
  public void testVerifyAnswerFreeTextCalibrationUserSkip() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID10), -1, "");
    assertFalse(result.getIsCorrect());
    assertEquals(
        "Learn something new today! The correct answer is answer", result.getMessage());
  }

  @Test
  public void testVerifyAnswerFreeTextCollectionUserSkip() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID11), -1, "");
    assertFalse(result.getIsCorrect());
    assertEquals(
        "No worries! This is a tough question! We are not sure about the answer either.",
        result.getMessage());
  }

  @Test
  public void testVerifyAnswerFreeTextCollectionUserAnswered() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID11), -1, "some answer ");
    assertTrue(result.getIsCorrect());
    assertEquals(
        "Well done! We don't know the answer either, and you are the first user to choose this "
        + "answer!", result.getMessage());
  }

  @Test
  public void testVerifyAnswerCalibrationCorrect() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID1), 0, "");
    assertTrue(result.getIsCorrect());
    assertEquals((Integer)0, result.getBestAnswer().getInternalID());
    assertEquals("Great! The correct answer is Answer 0", result.getMessage());
  }

  @Test
  public void testVerifyAnswerCalibrationInCorrect() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID1), 3, "");
    assertFalse(result.getIsCorrect());
    assertEquals((Integer)0, result.getBestAnswer().getInternalID());
    assertEquals("Sorry! The correct answer is Answer 0", result.getMessage());
  }

  @Test
  public void testVerifyAnswerCollectionCorrect() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID2), 0, "");
    assertTrue(result.getIsCorrect());
    assertEquals((Integer)0, result.getBestAnswer().getInternalID());
    assertEquals("Great! We are not 100% sure about the correct answer " +
                 "but we believe Answer 0 to be correct and 70% of the users agree.",
                 result.getMessage());
  }

  @Test
  public void testVerifyAnswerCollectionIncorrect() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID2), 2, "");
    assertFalse(result.getIsCorrect());
    assertEquals((Integer)0, result.getBestAnswer().getInternalID());
    assertEquals("Sorry! We are not 100% sure about the correct answer " +
                 "but we believe Answer 0 to be correct and 70% of the users agree.",
                 result.getMessage());
  }

  @Test
  public void testVerifyAnswerCollectionFirstAnswer() {
    // Creates a new question that was never answered before.
    Question question =
        new Question(
            QUIZ_ID1, new Text("test1"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, 12345L,
            QUESTION_CLIENT_ID2, false  /* not Gold */, true  /* is silver */, 1.5);
    for (int j = 0; j < 4; ++j) {
      question.addAnswer(new Answer(12345L, QUIZ_ID1, "Answer " + j, AnswerKind.SILVER, j));
    }
    questionService.save(question);

    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(12345L), 2, "");
    assertTrue(result.getIsCorrect());
    assertNull(result.getBestAnswer());
    assertEquals("Great! We are not 100% sure about the correct answer " +
                 "and you are the first user to answer!",
                 result.getMessage());
  }

  @Test
  public void testVerifyAnswerCollectionSkip() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID2), -1, "");
    assertFalse(result.getIsCorrect());
    assertEquals((Integer)0, result.getBestAnswer().getInternalID());
    assertEquals("Learn something new today! We are not 100% sure about the correct answer " +
                 "but we believe Answer 0 to be correct and 70% of the users agree.",
                 result.getMessage());
  }

  @Test
  public void testVerifyAnswerCalibrationSkip() {
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(QUESTION_ID1), -1, "");
    assertFalse(result.getIsCorrect());
    assertEquals((Integer)0, result.getBestAnswer().getInternalID());
    assertEquals("Learn something new today! The correct answer is Answer 0", result.getMessage());
  }

  @Test
  public void testVerifyAnswerCollectionFirstAnswerSkip() {
    // First, create a new question that has no probability correct in any of the answer.
    Question question =
        new Question(QUIZ_ID1, new Text("test1"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, 12345L,
                     QUESTION_CLIENT_ID2, false  /* not Gold */, true  /* is silver */, 1.5);
    for (int j = 0; j < 4; ++j) {
      question.addAnswer(new Answer(12345L, QUIZ_ID1, "Answer " + j, AnswerKind.SILVER, j));
    }
    questionService.save(question);

    // Then, skips this question.
    QuestionService.Result result = questionService.verifyAnswer(
        questionService.get(12345L), -1, "");
    assertFalse(result.getIsCorrect());
    assertNull(result.getBestAnswer());
    assertEquals("We are not 100% sure about the correct answer either " +
                 "and you are the first user to see this question!",
                 result.getMessage());
  }
}
