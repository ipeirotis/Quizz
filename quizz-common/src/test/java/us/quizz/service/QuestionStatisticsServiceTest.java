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

  // user 1 has  one correct, one incorrect answer
  double user1_quality = 1.0 * (0+1+1) / (2+4);
  // user1 without q1 has one correct out of 1
  double user1_quality_q1 = 1.0 * (1+1) / (1+4);
  
  
  // user 2 has  two correct answers
  double user2_quality = 1.0 * (1+1+1) / (2+4);
  // user2 without q1 has one correct
  double user2_quality_q1 = 1.0 * (1+1) / (1+4);
  
  // We do not put priors here, as all answers have the same prior
  // answer_0: User 1 did not pick it, user 2 picked it
  double prob_answer0 = (1-user1_quality_q1)/3 * (user2_quality_q1);
  // answer_1: Picked by user 1
  double prob_answer1 = user1_quality_q1 * (1-user2_quality_q1)/3;
  // answer 2 and 2: picked by none
  double prob_answer2 = (1-user1_quality_q1)/3 * (1-user2_quality_q1)/3;
  double prob_answer3 = (1-user1_quality_q1)/3 * (1-user2_quality_q1)/3;
  double denominator = (prob_answer0 + prob_answer1 + prob_answer2 + prob_answer3);

  
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

    assertEquals(Helper.getInformationGain(user1_quality, 4), question.getTotalUserScore(), 0.01);
    assertEquals(user1_quality, question.getConfidence(), 0.01);
    // It can be any of Answer 1/2/3
    // assertEquals("Answer 0", question.getLikelyAnswer());
    // Silver question, so this is null.
    assertNull(question.getIsLikelyAnswerCorrect());

    assertEquals(4, question.getAnswers().size());
    assertEquals(Helper.getInformationGain(user1_quality, 4), question.getAnswer(0).getBits(), 0.01);
    assertEquals((Integer)1, question.getAnswer(0).getNumberOfPicks());
    assertEquals(user1_quality, question.getAnswer(0).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(1).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(1).getNumberOfPicks());
    assertEquals((1-user1_quality)/3, question.getAnswer(1).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(2).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(2).getNumberOfPicks());
    assertEquals((1-user1_quality)/3, question.getAnswer(2).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(3).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(3).getNumberOfPicks());
    assertEquals((1-user1_quality)/3, question.getAnswer(3).getProbCorrect(), 0.01);
  }

  @Test
  public void testUpdateStatisticsForDuplicateAnswer() throws Exception {
    Question question = questionStatisticsService.updateStatistics("" + QUESTION_ID1);
    assertTrue(question.getHasUserAnswers());
    assertEquals((Integer)3, question.getNumberOfUserAnswers());
    assertEquals((Integer)2, question.getNumberOfCorrectUserAnswers());


    
    assertEquals(Helper.getInformationGain(user1_quality_q1, 4) + Helper.getInformationGain(user2_quality_q1, 4),
                 question.getTotalUserScore(), 0.01);

    
    assertEquals(prob_answer0 / denominator, question.getConfidence(), 0.01);
    assertEquals("Answer 1", question.getLikelyAnswer());
    assertFalse(question.getIsLikelyAnswerCorrect());

    assertEquals(4, question.getAnswers().size());
    assertEquals(Helper.getInformationGain(user1_quality_q1, 4), question.getAnswer(0).getBits(), 0.01);
    assertEquals((Integer)1, question.getAnswer(0).getNumberOfPicks());
    assertEquals(prob_answer0 / denominator, question.getAnswer(0).getProbCorrect(), 0.01);

    assertEquals(Helper.getInformationGain(user2_quality_q1, 4), question.getAnswer(1).getBits(), 0.01);
    assertEquals((Integer)1, question.getAnswer(1).getNumberOfPicks());
    assertEquals(prob_answer1 / denominator, question.getAnswer(1).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(2).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(2).getNumberOfPicks());
    assertEquals(prob_answer2 / denominator, question.getAnswer(2).getProbCorrect(), 0.01);

    assertEquals(0, question.getAnswer(3).getBits(), 0.01);
    assertEquals((Integer)0, question.getAnswer(3).getNumberOfPicks());
    assertEquals(prob_answer3 / denominator, question.getAnswer(3).getProbCorrect(), 0.01);
  }
}
