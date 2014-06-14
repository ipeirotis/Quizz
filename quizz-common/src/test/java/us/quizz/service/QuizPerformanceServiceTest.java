package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.QuestionKind;
import us.quizz.utils.QuizBaseTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(JUnit4.class)
public class QuizPerformanceServiceTest extends QuizBaseTest {
  private static final String TEST_QUIZ_ID = "test_quiz";
  private static final String TEST_USER_ID = "test_userid";
  private static final int NUM_CHOICES = 4;

  @Before
  public void setUp() {
    super.setUp();
    initQuizPerformanceServiceTest();
  }

  private void initQuizPerformanceServiceTest() {
    assertNotNull(getQuizPerformanceService());
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID1);
    quizPerformance.setScore(4.0);
    quizPerformance.setCorrectScore(1d);
    quizPerformance.setTotalScore(2d);
    quizPerformance.setCorrectanswers(1);
    quizPerformance.setIncorrectanswers(1);
    quizPerformance.setTotalanswers(4);
    quizPerformance.setTotalCalibrationAnswers(2);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID2);
    quizPerformance.setScore(4.0);
    quizPerformance.setCorrectScore(2d);
    quizPerformance.setTotalScore(2d);
    quizPerformance.setCorrectanswers(2);
    quizPerformance.setIncorrectanswers(0);
    quizPerformance.setTotalanswers(2);
    quizPerformance.setTotalCalibrationAnswers(2);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID2, USER_ID1);
    quizPerformance.setScore(0.8);
    quizPerformance.setCorrectanswers(1);
    quizPerformance.setIncorrectanswers(1);
    quizPerformance.setTotalanswers(2);
    quizPerformance.setTotalCalibrationAnswers(2);
    quizPerformanceService.save(quizPerformance);

    assertNotNull(getUserAnswerService());
    // Initializes those UserAnswer needed, which are those submitted by USER_ID1 in QUIZ_ID1.
    // These are used by the updateStatistics test.
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
    userAnswerRepository.save(
        new UserAnswer(
            USER_ID1, QUESTION_ID4, ANSWER_ID0, QUIZ_ID1, true, 4L, UserAnswer.SUBMIT));

    assertNotNull(getQuestionService());
    // Initializes those questions needed, which are QUESTION_ID1 to QUESTION_ID4 answered
    // by USER_ID1. These are used by the updateStatistics test.
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
  }

  @Test
  public void testGet() {
    QuizPerformance quizPerformance = quizPerformanceService.get(QUIZ_ID1, USER_ID1);
    assertEquals(QUIZ_ID1, quizPerformance.getQuiz());
    assertEquals(USER_ID1, quizPerformance.getUserid());
  }

  @Test
  public void testUpdateStatistics() {
    quizPerformanceService.updateStatistics(QUIZ_ID1, USER_ID1);
    assertNotNull(quizPerformanceService.get(QUIZ_ID1, USER_ID1));

    QuizPerformance quizPerformance = quizPerformanceService.get(QUIZ_ID1, USER_ID1);
    assertEquals((Integer)3, quizPerformance.getCorrectanswers());
    assertEquals((Integer)1, quizPerformance.getIncorrectanswers());
    assertEquals((Integer)4, quizPerformance.getTotalanswers());
    assertEquals((Integer)2, quizPerformance.getTotalCalibrationAnswers());

    // 4 * Helper.getInformationGain(2 / 6, NUM_CHOICES).
    assertEquals(3.1699, quizPerformance.getFreqInfoGain(), 0.01);

    // 4 * Helper.getBayesianMeanInformationGain(1, 2, NUM_CHOICES).
    assertEquals(2.613, quizPerformance.getBayesInfoGain(), 0.01);

    // 4 * (Helper.getBayesianMeanInformationGain(1, 2, NUM_CHOICES) -
    //      Math.sqrt(Helper.getBayesianVarianceInformationGain(1, 2, NUM_CHOICES))).
    assertEquals(1.046, quizPerformance.getLcbInfoGain(), 0.01);

    assertEquals((Integer)2, quizPerformance.getTotalUsers());
    assertEquals((Integer)2, quizPerformance.getRankScore());
  }

  @Test
  public void testDelete() {
    assertNotNull(quizPerformanceService.get(QUIZ_ID1, USER_ID1));
    quizPerformanceService.delete(QUIZ_ID1, USER_ID1);
    quizPerformanceService.flush();
    assertNull(quizPerformanceService.get(QUIZ_ID1, USER_ID1));
  }

  @Test
  public void testGetQuizPerformanceByQuiz() {
    List<QuizPerformance> quizPerformances =
        quizPerformanceService.getQuizPerformancesByQuiz(QUIZ_ID2);
    assertEquals(1, quizPerformances.size());

    // null quizID means get everything.
    quizPerformances = quizPerformanceService.getQuizPerformancesByQuiz(null);
    assertEquals(3, quizPerformances.size());
  }

  @Test
  public void testGetQuizPerformanceByUser() {
    List<QuizPerformance> quizPerformances =
        quizPerformanceService.getQuizPerformancesByUser(USER_ID1);
    assertEquals(2, quizPerformances.size());

    // null userID means get everything.
    quizPerformances = quizPerformanceService.getQuizPerformancesByUser(null);
    assertEquals(3, quizPerformances.size());
  }

  @Test
  public void testGetScoreSumByIds() {
    Set<String> ids = new HashSet<String>();
    ids.add(QuizPerformance.generateId(QUIZ_ID1, USER_ID1));
    ids.add(QuizPerformance.generateId(QUIZ_ID1, USER_ID2));
    assertEquals(8.0, quizPerformanceService.getScoreSumByIds(ids), 0.01);
  }

  @Test
  public void testGetScoreSumByIdsEmpty() {
    Set<String> ids = new HashSet<String>();
    assertEquals(0, quizPerformanceService.getScoreSumByIds(ids), 0.01);
  }

  @Test
  public void testGetScoreSumByIdsInvalid() {
    Set<String> ids = new HashSet<String>();
    ids.add(QuizPerformance.generateId(QUIZ_ID2, USER_ID2));
    assertEquals(0, quizPerformanceService.getScoreSumByIds(ids), 0.01);
  }

  @Test
  public void testComputeCorrect() throws Exception {
    List<Question> questions = getFakeMultipleChoiceQuestions(10, NUM_CHOICES, TEST_QUIZ_ID);
    List<UserAnswer> userAnswers = new ArrayList<UserAnswer>();
    // 5 correct answers.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        1  /* start */, 5  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 0  /* correct answerID */));
    // 5 incorrect answers.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        6  /* start */, 10  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 2  /* wrong answerID */));
    // 5 correct answers for collection questions.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        11  /* start */, 15  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 0  /* correct answerID */));
    // 5 incorrect answers for collection questions.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        16  /* start */, 20  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 1  /* incorrect answerID */));

    // This should be ignored from the stats.
    userAnswers.add(new UserAnswer(
        TEST_USER_ID, 21L, -1, TEST_QUIZ_ID, false, 1234L, UserAnswer.SKIP));

    QuizPerformance quizPerformance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    quizPerformanceService.computeCorrect(quizPerformance, userAnswers, questions);

    assertEquals((Integer)20, quizPerformance.getTotalanswers());
    assertEquals((Integer)10, quizPerformance.getCorrectanswers());
    assertEquals((Integer)10, quizPerformance.getTotalCalibrationAnswers());
    assertEquals((Integer)10, quizPerformance.getIncorrectanswers());

    // 20 * Helper.getInformationGain(0.5, NUM_CHOICES).
    assertEquals(4.15037, quizPerformance.getFreqInfoGain(), 0.01);

    // 20 * Helper.getBayesianMeanInformationGain(10, 20, NUM_CHOICES).
    assertEquals(3.36433, quizPerformance.getBayesInfoGain(), 0.01);

    // 20 * (Helper.getBayesianMeanInformationGain(10, 20, NUM_CHOICES) -
    //       Math.sqrt(Helper.getBayesianVarianceInformationGain(10, 20, NUM_CHOICES))).
    assertEquals(0.8786, quizPerformance.getLcbInfoGain(), 0.01);
  }

  @Test
  public void testDisplayPercentageCorrect() throws Exception {
    List<Question> questions = getFakeMultipleChoiceQuestions(10, NUM_CHOICES, TEST_QUIZ_ID);
    List<UserAnswer> userAnswers = new ArrayList<UserAnswer>();
    // 5 correct answers.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        1  /* start */, 5  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 0  /* correct answerID */));
    // 5 incorrect answers.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        6  /* start */, 10  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 2  /* wrong answerID */));

    // If the stats is not computed, we default to 0 first. 
    QuizPerformance quizPerformance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    assertEquals("0%", quizPerformance.displayPercentageCorrect());

    quizPerformance = quizPerformanceService.computeCorrect(
        quizPerformance, userAnswers, questions);
    assertEquals("50%", quizPerformance.displayPercentageCorrect());

    // 10 correct answers for collection questions and they still count.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        11  /* start */, 20  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 0  /* correct answerID */));
    quizPerformance = quizPerformanceService.computeCorrect(
        quizPerformance, userAnswers, questions);
    assertEquals("75%", quizPerformance.displayPercentageCorrect());
  }

  @Test
  public void testGetScore() throws Exception {
    List<Question> questions = getFakeMultipleChoiceQuestions(10, NUM_CHOICES, TEST_QUIZ_ID);
    List<UserAnswer> userAnswers = new ArrayList<UserAnswer>();
    // 5 correct answers.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        1  /* start */, 5  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 0  /* correct answerID */));
    // 5 incorrect answers.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        6  /* start */, 10  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 2  /* wrong answerID */));

    // If the stats is not computed, we default to 0 first.
    QuizPerformance quizPerformance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    assertEquals(0.0, quizPerformance.getScore(), 0.01);

    // Else, the score is the frequentist information gain.
    // 10 * Helper.getInformationGain(0.5, NUM_CHOICES).
    quizPerformance = quizPerformanceService.computeCorrect(
        quizPerformance, userAnswers, questions);
    assertEquals(2.07519, quizPerformance.getScore(), 0.01);
  }

  @Test
  public void testComputeRank() {
    Map<Integer, QuizPerformance> quizPerformances = getFakeQuizPerformances(10, TEST_QUIZ_ID);
    List<QuizPerformance> performancesList = new ArrayList<QuizPerformance>();
    performancesList.addAll(quizPerformances.values());

    QuizPerformance worstPerformer = quizPerformances.get(1);
    worstPerformer = quizPerformanceService.computeRank(worstPerformer, performancesList);
    assertEquals(10, worstPerformer.getRankScore(), 0.01);
    assertEquals((Integer)10, worstPerformer.getTotalUsers());

    QuizPerformance bestPerformer = quizPerformances.get(10);
    bestPerformer = quizPerformanceService.computeRank(bestPerformer, performancesList);
    assertEquals(1, bestPerformer.getRankScore(), 0.01);
    assertEquals((Integer)10, bestPerformer.getTotalUsers());
  }

  @Test
  public void testComputeRankSameScore() {
    Map<Integer, QuizPerformance> quizPerformances = getFakeQuizPerformances(9, TEST_QUIZ_ID);
    List<QuizPerformance> performancesList = new ArrayList<QuizPerformance>();
    performancesList.addAll(quizPerformances.values());

    // There will be two users with the same score of 5.
    QuizPerformance testPerformer = new QuizPerformance(TEST_QUIZ_ID, "10");
    testPerformer.setScore((double) 5);

    performancesList.add(testPerformer);
    testPerformer = quizPerformanceService.computeRank(testPerformer, performancesList);
    assertEquals(5, testPerformer.getRankScore(), 0.01);
    assertEquals((Integer)10, testPerformer.getTotalUsers());

    testPerformer = quizPerformances.get(4);
    testPerformer = quizPerformanceService.computeRank(testPerformer, performancesList);
    assertEquals(7, testPerformer.getRankScore(), 0.01);
    assertEquals((Integer)10, testPerformer.getTotalUsers());
  }

  @Test
  public void testDisplayRankScore() throws Exception {
    Map<Integer, QuizPerformance> quizPerformances = getFakeQuizPerformances(5, TEST_QUIZ_ID);
    List<QuizPerformance> performancesList = new ArrayList<QuizPerformance>();

    QuizPerformance testPerformance = quizPerformances.get(3);
    // Null rank score.
    assertEquals("--", testPerformance.displayRankScore());

    // Total users = 0.
    testPerformance = quizPerformanceService.computeRank(testPerformance, performancesList);
    assertEquals("--", testPerformance.displayRankScore());

    performancesList.addAll(quizPerformances.values());
    testPerformance = quizPerformanceService.computeRank(testPerformance, performancesList);
    assertEquals("60%", testPerformance.displayRankScore());
  }
}
