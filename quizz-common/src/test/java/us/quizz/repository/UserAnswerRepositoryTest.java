package us.quizz.repository;

import static org.junit.Assert.assertEquals;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.UserAnswer;

import java.util.List;
import java.util.Set;

@RunWith(JUnit4.class)
public class UserAnswerRepositoryTest {
  private static final String USERID1 = "userid_1";
  private static final String USERID2 = "userid_2";

  private static final String QUIZ_ID1 = "quizid_1";
  private static final String QUIZ_ID2 = "quizid_2";

  private UserAnswerRepository userAnswerRepository = null;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
          new LocalTaskQueueTestConfig(),
          new LocalMemcacheServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
    userAnswerRepository = new UserAnswerRepository();

    for (int i = 1; i <= 100; ++i) {
      UserAnswer userAnswer = new UserAnswer(USERID1, (long) i, i % 4,
          i % 2 == 0 ? QUIZ_ID1 : QUIZ_ID2);
      userAnswer.setAction(i % 5 == 0 ? "I don't know" : UserAnswer.SUBMIT);
      userAnswer.setIsCorrect(i % 5 == 1 || i % 5 == 0 ? false : true);
      userAnswerRepository.save(userAnswer);

      userAnswer = new UserAnswer(USERID2, (long) i, i % 2,
          i % 2 == 0 ? QUIZ_ID1 : QUIZ_ID2);
      userAnswer.setAction(i % 5 == 0 ? "I don't know" : UserAnswer.SUBMIT);
      userAnswer.setIsCorrect(i % 5 == 2 || i % 5 == 0 ? false : true);
      userAnswerRepository.save(userAnswer);
    }
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testGetUserAnswersForQuiz() {
    List<UserAnswer> results = userAnswerRepository.getUserAnswersForQuiz(QUIZ_ID1);
    assertEquals(100, results.size());
    for (UserAnswer answer : results) {
      assertEquals(QUIZ_ID1, answer.getQuizID());
    }
  }

  @Test
  public void testGetUserAnswers() {
    List<UserAnswer> results = userAnswerRepository.getUserAnswers(QUIZ_ID1, USERID1);
    assertEquals(50, results.size());
    for (UserAnswer answer : results) {
      assertEquals(QUIZ_ID1, answer.getQuizID());
      assertEquals(USERID1, answer.getUserid());
    }
  }

  @Test
  public void testGetUserAnswersForQuestion() {
    List<UserAnswer> results = userAnswerRepository.getUserAnswersForQuestion(10L);
    assertEquals(2, results.size());
    assertEquals((Long)10L, results.get(0).getQuestionID());
    assertEquals((Long)10L, results.get(1).getQuestionID());

    results = userAnswerRepository.getUserAnswersForQuestion(102L);
    assertEquals(0, results.size());
  }

  @Test
  public void testGetSubmittedUserAnswersForQuestion() {
    List<UserAnswer> results = userAnswerRepository.getSubmittedUserAnswersForQuestion(3L);
    assertEquals(2, results.size());
    assertEquals((Long)3L, results.get(0).getQuestionID());
    assertEquals((Long)3L, results.get(1).getQuestionID());
    
    results = userAnswerRepository.getSubmittedUserAnswersForQuestion(5L);
    assertEquals(0, results.size());
  }

  @Test
  public void testGetNumberOfUserAnswersExcludingIDK() {
    assertEquals(0, userAnswerRepository.getNumberOfUserAnswersExcludingIDK(5L));
    assertEquals(2, userAnswerRepository.getNumberOfUserAnswersExcludingIDK(3L));
  }

  @Test
  public void testGetNumberOfCorrectUserAnswers() {
    assertEquals(0, userAnswerRepository.getNumberOfCorrectUserAnswers(5L));
    assertEquals(1, userAnswerRepository.getNumberOfCorrectUserAnswers(12L));
    assertEquals(2, userAnswerRepository.getNumberOfCorrectUserAnswers(13L));
  }

  @Test
  public void testGetNumberOfUserAnswers() {
    assertEquals((Integer)100, userAnswerRepository.getNumberOfUserAnswers(QUIZ_ID1));
  }

  @Test
  public void testGetUserIDs() {
    Set<String> userIDs = userAnswerRepository.getUserIDs(QUIZ_ID1);
    assertEquals(2, userIDs.size());
  }
}
