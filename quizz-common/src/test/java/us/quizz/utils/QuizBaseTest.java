package us.quizz.utils;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import org.junit.After;
import org.junit.Before;

import us.quizz.entities.Question;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;

import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

// A test util class to do all the common initialization of persistent manager and repositories
// construction and content.
public class QuizBaseTest {
  protected static final String USER_ID1 = "1001";
  protected static final String USER_ID2 = "1002";
  protected static final String QUIZ_ID1 = "quizid_1";
  protected static final String QUIZ_ID2 = "quizid_2";
  protected static final Long QUESTION_ID1 = 2001L;
  protected static final Long QUESTION_ID2 = 2002L;
  protected static final Long QUESTION_ID3 = 2003L;
  protected static final Long QUESTION_ID4 = 2004L;
  protected static final Long QUESTION_ID5 = 2005L;
  protected static final Long QUESTION_ID6 = 2006L;
  protected static final Long QUESTION_ID7 = 2007L;
  protected static final Long QUESTION_ID8 = 2008L;
  protected static final Long QUESTION_ID9 = 2009L;
  protected static final String QUESTION_CLIENT_ID1 = "qclient_1";
  protected static final String QUESTION_CLIENT_ID2 = "qclient_2";
  protected static final String QUESTION_CLIENT_ID3 = "qclient_3";
  protected static final String QUESTION_CLIENT_ID4 = "qclient_4";
  protected static final int ANSWER_ID1 = 1;
  protected static final int ANSWER_ID2 = 2;

  protected PersistenceManager persistenceManager;
  protected PersistenceManager actualPersistenceManager;
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(), 
          new LocalTaskQueueTestConfig(),
          new LocalMemcacheServiceTestConfig());

  protected QuizPerformanceRepository quizPerformanceRepository = null;
  protected QuizQuestionRepository quizQuestionRepository = null;
  protected QuizRepository quizRepository = null;
  protected UserAnswerRepository userAnswerRepository = null;
  protected UserReferralRepository userReferralRepository = null;

  @Before
  public void setUp() {
    helper.setUp();
    quizPerformanceRepository = null;
    quizQuestionRepository = null;
    quizRepository = null;
    userAnswerRepository = null;
    userReferralRepository = null;
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  protected void initPersistenceManager() {
    Properties newProperties = new Properties();
    newProperties.put("javax.jdo.PersistenceManagerFactoryClass",
                      "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
    newProperties.put("javax.jdo.option.ConnectionURL", "appengine");
    newProperties.put("javax.jdo.option.NontransactionalRead", "true");
    newProperties.put("javax.jdo.option.NontransactionalWrite", "true");
    newProperties.put("javax.jdo.option.RetainValues", "true");
    newProperties.put("datanucleus.appengine.autoCreateDatastoreTxns", "true");
    newProperties.put("datanucleus.appengine.allowMultipleRelationsOfSameType", "true");

    PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(newProperties);
    actualPersistenceManager = pmf.getPersistenceManager();
    persistenceManager = spy(actualPersistenceManager);
    doNothing().when(persistenceManager).close();
  }

  protected PersistenceManager getPersistenceManager() {
    return persistenceManager;
  }

  protected UserAnswerRepository getUserAnswerRepository() {
    if (userAnswerRepository == null) {
      userAnswerRepository = spy(new UserAnswerRepository());
      when(userAnswerRepository.getPersistenceManager()).thenReturn(getPersistenceManager());
    }
    return userAnswerRepository;
  }

  protected UserReferralRepository getUserReferralRepository() {
    if (userReferralRepository == null) {
      userReferralRepository = spy(new UserReferralRepository());
      when(userReferralRepository.getPersistenceManager()).thenReturn(getPersistenceManager());
    }
    return userReferralRepository;
  }

  protected QuizPerformanceRepository getQuizPerformanceRepository() {
    if (quizPerformanceRepository == null) {
      quizPerformanceRepository = spy(new QuizPerformanceRepository());
      when(quizPerformanceRepository.getPersistenceManager()).thenReturn(getPersistenceManager());
    }
    return quizPerformanceRepository;
  }

  protected QuizRepository getQuizRepository() {
    if (quizRepository == null) {
      quizRepository = spy(new QuizRepository());
    }
    return quizRepository;
  }

  protected QuizQuestionRepository getQuizQuestionRepository() {
    if (quizQuestionRepository == null) {
      quizQuestionRepository = spy(new QuizQuestionRepository(getQuizRepository(),
                                                              getUserAnswerRepository()));
      when(quizQuestionRepository.getPersistenceManager()).thenReturn(getPersistenceManager());
    }
    return quizQuestionRepository;
  }

  protected void initUserAnswerRepository() {
    assertNotNull(getUserAnswerRepository());

    // User 1 answers 3 questions from a single quiz, 2 of which are duplicate questions.
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID1, ANSWER_ID1, QUIZ_ID1));
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID2, ANSWER_ID2, QUIZ_ID1));
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID2, ANSWER_ID1, QUIZ_ID1));
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID3, ANSWER_ID2, QUIZ_ID1));

    // User 2 answers 0 questions.
  }

  protected void initQuizQuestionRepository() {
    assertNotNull(getQuizQuestionRepository());
    initUserAnswerRepository();

    // Quiz 1 has 5 questions, 2 are calibration, 3 are collections.
    // Question 1 and 4 have the same client id.
    quizQuestionRepository.save(
        new Question(QUIZ_ID1, "test1", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID1, QUESTION_CLIENT_ID1,
                     true  /* is Gold */, false  /* Not silver */));
    quizQuestionRepository.save(
        new Question(QUIZ_ID1, "test2", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID2, QUESTION_CLIENT_ID2,
                     false, true));
    quizQuestionRepository.save(
        new Question(QUIZ_ID1, "test3", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID3, QUESTION_CLIENT_ID3,
                     false, true));
    quizQuestionRepository.save(
        new Question(QUIZ_ID1, "test4", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID4, QUESTION_CLIENT_ID1,
                     true, false));
    quizQuestionRepository.save(
        new Question(QUIZ_ID1, "test5", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID5, QUESTION_CLIENT_ID4,
                     false, true));

    // Quiz 2 has 4 questions, 1 is calibration, 3 are collections.
    // All the questions have null or empty client id.
    quizQuestionRepository.save(
        new Question(QUIZ_ID2, "test6", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID6, "",
                     true, false));
    quizQuestionRepository.save(
        new Question(QUIZ_ID2, "test7", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID7, "",
                     false, true));
    quizQuestionRepository.save(
        new Question(QUIZ_ID2, "test8", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID8, null,
                     false, true));
    quizQuestionRepository.save(
        new Question(QUIZ_ID2, "test9", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID9, null,
                     false, true));
  }
}
