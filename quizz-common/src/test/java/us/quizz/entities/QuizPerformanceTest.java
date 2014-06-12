package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.service.QuestionStatisticsService;
import us.quizz.utils.QuizBaseTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class QuizPerformanceTest extends QuizBaseTest {
  private static final String TEST_QUIZ_ID = "test_quiz";
  private static final String TEST_USER_ID = "test_userid";
  private static final int NUM_CHOICES = 4;

  @Before
  public void setUp() {
    super.setUp();

    initUserAnswerService();
    initQuizPerformanceService();
    initQuestionService();
    
  }

  
  @Test
  public void testConstructor() {
    QuizPerformance quiz_performance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    assertEquals(TEST_QUIZ_ID, quiz_performance.getQuiz());
    assertEquals(TEST_USER_ID, quiz_performance.getUserid());
    Integer zero = 0;
    assertEquals(zero, quiz_performance.getTotalanswers());
    assertEquals(zero, quiz_performance.getTotalCalibrationAnswers());
    assertEquals(zero, quiz_performance.getCorrectanswers());
    assertEquals(zero, quiz_performance.getIncorrectanswers());
    assertEquals(0.0, quiz_performance.getScore(), 0.01);

    assertEquals(TEST_USER_ID + "_" + TEST_QUIZ_ID, quiz_performance.getId());
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

    QuizPerformance quiz_performance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    quiz_performance = quizPerformanceService.computeCorrect(quiz_performance, userAnswers, questions);

    assertEquals((Integer)20, quiz_performance.getTotalanswers());
    assertEquals((Integer)10, quiz_performance.getCorrectanswers());
    assertEquals((Integer)10, quiz_performance.getTotalCalibrationAnswers());
    assertEquals((Integer)10, quiz_performance.getIncorrectanswers());

    // 20 * Helper.getInformationGain(0.5, NUM_CHOICES).
    assertEquals(4.15037, quiz_performance.getFreqInfoGain(), 0.01);

    // 20 * Helper.getBayesianMeanInformationGain(10, 20, NUM_CHOICES).
    assertEquals(3.36433, quiz_performance.getBayesInfoGain(), 0.01);

    // 20 * (Helper.getBayesianMeanInformationGain(10, 20, NUM_CHOICES) -
    //       Math.sqrt(Helper.getBayesianVarianceInformationGain(10, 20, NUM_CHOICES))).
    assertEquals(0.8786, quiz_performance.getLcbInfoGain(), 0.01);
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
    QuizPerformance quiz_performance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    assertEquals("0%", quiz_performance.displayPercentageCorrect());

    quiz_performance = quizPerformanceService.computeCorrect(quiz_performance, userAnswers, questions);
    assertEquals("50%", quiz_performance.displayPercentageCorrect());

    // 10 correct answers for collection questions and they still count.
    userAnswers.addAll(getFakeMultipleChoiceUserAnswers(
        11  /* start */, 20  /* end */, TEST_USER_ID, TEST_QUIZ_ID, 0  /* correct answerID */));
    quiz_performance = quizPerformanceService.computeCorrect(quiz_performance, userAnswers, questions);
    assertEquals("75%", quiz_performance.displayPercentageCorrect());
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
    QuizPerformance quiz_performance = new QuizPerformance(TEST_QUIZ_ID, TEST_USER_ID);
    assertEquals(0.0, quiz_performance.getScore(), 0.01);

    // Else, the score is the frequentist information gain.
    // 10 * Helper.getInformationGain(0.5, NUM_CHOICES).
    quiz_performance = quizPerformanceService.computeCorrect(quiz_performance, userAnswers, questions);
    assertEquals(2.07519, quiz_performance.getScore(), 0.01);
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
