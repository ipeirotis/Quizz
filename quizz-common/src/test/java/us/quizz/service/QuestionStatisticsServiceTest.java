package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.QuestionKind;
import us.quizz.utils.Helper;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class QuestionStatisticsServiceTest extends QuizBaseTest {
  private QuestionStatisticsService questionStatisticsService;

  @Before
  public void setUp() {
    super.setUp();
    initQuestionStatisticsService();
  }

  public void initQuestionStatisticsService() {
    assertNotNull(getUserAnswerService());

    // Only initializes what we need, which are the UserAnswer for QUESTION_ID1 and QUESTION_ID2.
    // USER_ID1 answers three questions, two of which are duplicate QUESTION_ID1, so they only
    // count once.
    userAnswerRepository.save(
        new UserAnswer(USER_ID1, QUESTION_ID2, ANSWER_ID0, QUIZ_ID1, true, 1L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(USER_ID1, QUESTION_ID1, ANSWER_ID3, QUIZ_ID1, false, 2L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(USER_ID1, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true, 3L, UserAnswer.SUBMIT));
    // USER_ID2 only answers QUESTION_ID1, but not QUESTION_ID2.
    userAnswerRepository.save(
        new UserAnswer(USER_ID2, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true, 4L, UserAnswer.SUBMIT));

    // Only initializes the fields that are needed for the QuizPerformance.
    assertNotNull(getQuizPerformanceService());
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID1);
    quizPerformance.setCorrectScore(4d);
    quizPerformance.setTotalScore(6d);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID2);
    quizPerformance.setCorrectScore(2d);
    quizPerformance.setTotalScore(2d);
    quizPerformanceService.save(quizPerformance);

    assertNotNull(getQuestionService());
    Question question = new Question(
        QUIZ_ID1, new Text("test1"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID1,
        QUESTION_CLIENT_ID1, true  /* is Gold */, false  /* Not silver */, 1.5);
    addAnswers(question, QUESTION_ID1, 4, QUIZ_ID1, true);
    questionService.save(question);
    question = new Question(
        QUIZ_ID1, new Text("test2"), QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID2,
        QUESTION_CLIENT_ID2, false, true, 0.9);
    addAnswers(question, QUESTION_ID2, 4, QUIZ_ID1, false);
    questionService.save(question);

    questionStatisticsService = new QuestionStatisticsService(
        questionService, userAnswerService, quizPerformanceService);
  }

  @Test
  public void testUpdateStatistics() throws Exception {
    Question question = questionStatisticsService.updateStatistics("" + QUESTION_ID2);
    assertTrue(question.getHasUserAnswers());
    // QUESTION_ID2 is only answered by USER_1.
    assertEquals((Integer)1, question.getNumberOfUserAnswers());
    assertEquals((Integer)1, question.getNumberOfCorrectUserAnswers());

    int numChoices = 4;
    assertEquals(numChoices, question.getAnswers().size());

    // userProb = (correctScore + 1) / (totalScore + numChoices).
    // user 1 has 4 correct out of 6 answers.
    double user1Quality = 1.0 * (4 + 1) / (6 + numChoices);

    // QUESTION_ID2 is a silver question, so this is null.
    assertNull(question.getIsLikelyAnswerCorrect());

    // There is only one user who picks answer 0 for QUESTION_ID2, the rest of answers are never
    // picked.
    assertEquals((Integer)1, question.getAnswer(0).getNumberOfPicks());
    assertEquals((Integer)0, question.getAnswer(1).getNumberOfPicks());
    assertEquals((Integer)0, question.getAnswer(2).getNumberOfPicks());
    assertEquals((Integer)0, question.getAnswer(3).getNumberOfPicks());

    // Answer 0 is chosen by the user, so its bit is based on its user prob.
    // The rest of the answers have 0 bit since they are never chosen.
    // QUESTION_ID2 is a collection question, so we take the original user quality, not the one
    // discounted with the current question.
    assertEquals(
        Helper.getInformationGain(user1Quality, numChoices), question.getAnswer(0).getBits(), 0.01);
    assertEquals(0, question.getAnswer(1).getBits(), 0.01);
    assertEquals(0, question.getAnswer(2).getBits(), 0.01);
    assertEquals(0, question.getAnswer(3).getBits(), 0.01);

    // Total user score is just the sum of bits of each answer.
    assertEquals(
        Helper.getInformationGain(user1Quality, numChoices), question.getTotalUserScore(), 0.01);

    // The answer chosen by user will have probability = userProb.
    assertEquals(user1Quality, question.getAnswer(0).getProbCorrect(), 0.01);
    // While each of the other answer will have probability (1 - userProb) / 3.
    assertEquals((1 - user1Quality) / 3, question.getAnswer(1).getProbCorrect(), 0.01);
    assertEquals((1 - user1Quality) / 3, question.getAnswer(2).getProbCorrect(), 0.01);
    assertEquals((1 - user1Quality) / 3, question.getAnswer(3).getProbCorrect(), 0.01);

    // The question confidence is the highest probCorrect.
    assertEquals(user1Quality, question.getConfidence(), 0.01);
    assertEquals("Answer 0", question.getLikelyAnswer());
  }

  @Test
  public void testUpdateStatisticsForDuplicateAnswer() throws Exception {
    Question question = questionStatisticsService.updateStatistics("" + QUESTION_ID1);
    assertTrue(question.getHasUserAnswers());
    // Two answers are from USER_1 and the other answer is from USER_2.
    // But one answer (second answer of USER_1) is a duplicate, so we ignore it.
    assertEquals((Integer)2, question.getNumberOfUserAnswers());
    assertEquals((Integer)1, question.getNumberOfCorrectUserAnswers());

    int numChoices = 4;
    assertEquals(numChoices, question.getAnswers().size());

    // userProb = (correctScore + 1) / (totalScore + numChoices).
    // Since Q1 is a calibration question, we remove it from the influence of the userProb.
    // user1 without q1 has 4 correct out of 5. Note that user1 actually answers question 1
    // twice, but we only takes its first answer response, which is a wrong.
    double user1QualityWithoutQ1 = 1.0 * (4 + 1) / (5 + numChoices);

    // user 2 without q1 has only 1 correct.
    double user2QualityWithoutQ1 = 1.0 * (1 + 1) / (1 + numChoices);

    // USER_1 picks answer 3 first, while USER_2 picks answer 0 once, the rest of answers
    // are never picked.
    assertEquals((Integer)1, question.getAnswer(0).getNumberOfPicks());
    assertEquals((Integer)0, question.getAnswer(1).getNumberOfPicks());
    assertEquals((Integer)0, question.getAnswer(2).getNumberOfPicks());
    assertEquals((Integer)1, question.getAnswer(3).getNumberOfPicks());

    // Answer 0 is picked by USER_2 while Answer 3 is picked by USER_1 so the bits are based on
    // information gain of each user.
    // The rest of the answers are never picked and thus have 0 bit.
    assertEquals(
        Helper.getInformationGain(user2QualityWithoutQ1, numChoices),
        question.getAnswer(0).getBits(), 0.01);
    assertEquals(0, question.getAnswer(1).getBits(), 0.01);
    assertEquals(0, question.getAnswer(2).getBits(), 0.01);
    assertEquals(
        Helper.getInformationGain(user1QualityWithoutQ1, numChoices),
        question.getAnswer(3).getBits(), 0.01);

    // The total user score is just the sum of all answer bits.
    assertEquals(
        Helper.getInformationGain(user1QualityWithoutQ1, numChoices)
            + Helper.getInformationGain(user2QualityWithoutQ1, numChoices),
        question.getTotalUserScore(), 0.01);

    // Each answer's probability is the product of user probability for each user who picks it and
    // (1 - user probability) / (numChoices - 1) for each user who doesn't pick it.
    // answer_0: User 1 did not pick it, user 2 picked it.
    double probAnswer0 = (1 - user1QualityWithoutQ1) / 3 * (user2QualityWithoutQ1);
    // answer_3: Picked by user 1, but not user 2.
    double probAnswer3 = user1QualityWithoutQ1 * (1 - user2QualityWithoutQ1) / 3;
    // answer 1 and 2: picked by none.
    double probAnswer1 = (1 - user1QualityWithoutQ1) / 3 * (1 - user2QualityWithoutQ1) / 3;
    double probAnswer2 = (1 - user1QualityWithoutQ1) / 3 * (1 - user2QualityWithoutQ1) / 3;
    double totalUnnormalizedProb = (probAnswer0 + probAnswer1 + probAnswer2 + probAnswer3);
    assertEquals(probAnswer0 / totalUnnormalizedProb, question.getAnswer(0).getProbCorrect(), 0.01);
    assertEquals(probAnswer1 / totalUnnormalizedProb, question.getAnswer(1).getProbCorrect(), 0.01);
    assertEquals(probAnswer2 / totalUnnormalizedProb, question.getAnswer(2).getProbCorrect(), 0.01);
    assertEquals(probAnswer3 / totalUnnormalizedProb, question.getAnswer(3).getProbCorrect(), 0.01);

    // The question confidence is the highest probCorrect.
    assertEquals(probAnswer3 / totalUnnormalizedProb, question.getConfidence(), 0.01);
    assertFalse(question.getIsLikelyAnswerCorrect());
    assertEquals("Answer 3", question.getLikelyAnswer());
  }

  @Test
  public void testUpdateStatisticsQuestionDifficultyDuplicate() throws Exception {
    userAnswerRepository.save(
        new UserAnswer(USER_ID3, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true, 2L, UserAnswer.SUBMIT));
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID3);
    quizPerformance.setCorrectScore(4d);
    quizPerformance.setTotalScore(2d);
    quizPerformanceService.save(quizPerformance);

    // Question 1 is a calibration question and has duplicate answers from USER_1, we take only
    // its first answer, which is incorrect. USER_2 and USER_3 answers it correctly.
    Question question1  = questionStatisticsService.updateStatistics("" + QUESTION_ID1);
    assertEquals(0.333, question1.getDifficulty(), 0.01);
  }

  @Test
  public void testUpdateStatisticsQuestionDifficultyPerfect() throws Exception {
    // Question 2 is a collection question and only answered once by USER_1, correctly.
    Question question2 = questionStatisticsService.updateStatistics("" + QUESTION_ID2);
    assertEquals(0.0, question2.getDifficulty(), 0.0);
  }

  @Test
  public void testUpdateStatisticsQuestionDifficultyPrior() throws Exception {
    Question newQuestion = new Question(
        QUIZ_ID1, new Text("test3"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID3,
        QUESTION_CLIENT_ID3, true  /* is Gold */, false  /* Not silver */, 1.5);
    addAnswers(newQuestion, QUESTION_ID3, 4, QUIZ_ID1, true);
    questionService.save(newQuestion);

    // Question 3 has not been answered by anyone, so we use the prior.
    Question question3 = questionStatisticsService.updateStatistics("" + QUESTION_ID3);
    assertEquals(question3.getDifficultyPrior(), question3.getDifficulty(), 0.01);
  }

  @Test
  public void testUpdateStatisticsQuestionDifficultyHard() throws Exception {
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID3); 
    quizPerformance.setCorrectScore(4d);
    quizPerformance.setTotalScore(2d);
    quizPerformanceService.save(quizPerformance);

    Question newQuestion = new Question(
        QUIZ_ID1, new Text("test4"), QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID4,
        QUESTION_CLIENT_ID4, true, false, 0.9);
    addAnswers(newQuestion, QUESTION_ID4, 4, QUIZ_ID1, true);
    questionService.save(newQuestion);

    userAnswerRepository.save(
        new UserAnswer(USER_ID2, QUESTION_ID4, ANSWER_ID3, QUIZ_ID1, false, 2L, UserAnswer.SUBMIT));
    userAnswerRepository.save(
        new UserAnswer(USER_ID3, QUESTION_ID4, ANSWER_ID2, QUIZ_ID1, false, 2L, UserAnswer.SUBMIT));

    // Question 4 is answered incorrectly by everyone.
    Question question4 = questionStatisticsService.updateStatistics("" + QUESTION_ID4);
    assertEquals(1.0, question4.getDifficulty(), 0.00);
  }
}
