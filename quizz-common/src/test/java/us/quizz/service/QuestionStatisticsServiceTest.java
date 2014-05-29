package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Question;
import us.quizz.utils.Helper;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class QuestionStatisticsServiceTest extends QuizBaseTest {
  private QuestionStatisticsService questionStatisticsService;

  @Before
  public void setUp() {
    super.setUp();

    initUserAnswerService();
    initQuizPerformanceService();
    initQuestionService();
    questionStatisticsService = new QuestionStatisticsService(
        questionService, userAnswerService, quizPerformanceService);
  }

  @Test
  public void testUpdateStatistics() throws Exception {
    Question question = questionStatisticsService.updateStatistics("" + QUESTION_ID2);
    assertTrue(question.getHasUserAnswers());
    assertEquals((Integer)1, question.getNumberOfUserAnswers());
    assertEquals((Integer)1, question.getNumberOfCorrectUserAnswers());

    // userProb = 0.5 for USER_ID1.
    assertEquals(Helper.getInformationGain(0.5, 4), question.getTotalUserScore(), 0.01);
    // 0.5 * 0.25 / (0.5 * 0.25 + 3 * (0.25 * 0.5 / 3))
    assertEquals(0.5, question.getConfidence(), 0.01);
    assertEquals("Answer 0", question.getLikelyAnswer());
    // Silver question, so this is null.
    assertNull(question.getIsLikelyAnswerCorrect());

    assertEquals(4, question.getAnswers().size());
    assertEquals(Helper.getInformationGain(0.5, 4), question.getAnswer(0).getBits(), 0.01);
    assertEquals((Integer)1, question.getAnswer(0).getNumberOfPicks());
    assertEquals(0.5, question.getAnswer(0).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(1).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(1).getNumberOfPicks());
    assertEquals(0.1667, question.getAnswer(1).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(2).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(2).getNumberOfPicks());
    assertEquals(0.1667, question.getAnswer(2).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(3).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(3).getNumberOfPicks());
    assertEquals(0.1667, question.getAnswer(3).getProbCorrect(), 0.01);
  }

  @Test
  public void testUpdateStatisticsForDuplicateAnswer() throws Exception {
    Question question = questionStatisticsService.updateStatistics("" + QUESTION_ID1);
    assertTrue(question.getHasUserAnswers());
    assertEquals((Integer)3, question.getNumberOfUserAnswers());
    assertEquals((Integer)2, question.getNumberOfCorrectUserAnswers());

    // userProb = 0.5 for USER_ID1, userProb = 0.4 for USER_ID2 but after removing the effect
    // of the QUESTION 1, it becomes 0.574 for USER_ID1 and 0.25 for USER_ID2.
    assertEquals(Helper.getInformationGain(0.5714, 4) + Helper.getInformationGain(0.25, 4),
                 question.getTotalUserScore(), 0.01);

    // USER_ID1 answers first before the USER_ID2.
    // answer_0: 0.25 * (0.4286 / 3) * 0.25.
    // answer_1: 0.25 * 0.5714 * (0.75 / 3).
    // answer_2 and answer_3: 0.25 * (0.4286 / 3) * (0.75 / 3).
    assertEquals(0.5714, question.getConfidence(), 0.01);
    assertEquals("Answer 1", question.getLikelyAnswer());
    assertFalse(question.getIsLikelyAnswerCorrect());

    assertEquals(4, question.getAnswers().size());
    assertEquals(Helper.getInformationGain(0.25, 4), question.getAnswer(0).getBits(), 0.01);
    assertEquals((Integer)1, question.getAnswer(0).getNumberOfPicks());
    assertEquals(0.14287, question.getAnswer(0).getProbCorrect(), 0.01);

    assertEquals(Helper.getInformationGain(0.5714, 4), question.getAnswer(1).getBits(), 0.01);
    assertEquals((Integer)1, question.getAnswer(1).getNumberOfPicks());
    assertEquals(0.5714, question.getAnswer(1).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(2).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(2).getNumberOfPicks());
    assertEquals(0.14287, question.getAnswer(2).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(3).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(3).getNumberOfPicks());
    assertEquals(0.14287, question.getAnswer(3).getProbCorrect(), 0.01);
  }
}
