package us.quizz.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import org.junit.After;
import org.junit.Before;

import us.quizz.entities.Answer;
import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.BadgeAssignmentRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.QuestionRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.service.AnswerChallengeCounterService;
import us.quizz.service.BadgeAssignmentService;
import us.quizz.service.BadgeService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizPerformanceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  protected static final String BADGE_NAME1 = "5 Correct";
  protected static final String BADGE_SHORTNAME1 = "5C";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(), 
          new LocalTaskQueueTestConfig(),
          new LocalMemcacheServiceTestConfig());

  protected AnswerChallengeCounterRepository answerChallengeCounterRepository = null;
  protected BadgeAssignmentRepository badgeAssignmentRepository = null;
  protected BadgeRepository badgeRepository = null;
  protected QuestionRepository questionRepository = null;
  protected QuizPerformanceRepository quizPerformanceRepository = null;
  protected QuizRepository quizRepository = null;
  protected UserAnswerRepository userAnswerRepository = null;
  protected UserReferralRepository userReferralRepository = null;

  protected AnswerChallengeCounterService answerChallengeCounterService = null;
  protected BadgeAssignmentService badgeAssignmentService = null;
  protected BadgeService badgeService = null;
  protected QuestionService questionService = null;
  protected QuizPerformanceService quizPerformanceService = null;

  @Before
  public void setUp() {
    helper.setUp();
    answerChallengeCounterRepository = null;
    badgeAssignmentRepository = null;
    badgeRepository = null;
    questionRepository = null;
    quizPerformanceRepository = null;
    quizRepository = null;
    userAnswerRepository = null;
    userReferralRepository = null;

    answerChallengeCounterService = null;
    badgeAssignmentService = null;
    badgeService = null;
    questionService = null;
    quizPerformanceService = null;
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  protected AnswerChallengeCounterRepository getAnswerChallengeCounterRepository() {
    if (answerChallengeCounterRepository == null) {
      answerChallengeCounterRepository = new AnswerChallengeCounterRepository();
    }
    return answerChallengeCounterRepository;
  }

  protected BadgeAssignmentRepository getBadgeAssignmentRepository() {
    if (badgeAssignmentRepository == null) {
      badgeAssignmentRepository = new BadgeAssignmentRepository();
    } 
    return badgeAssignmentRepository;
  }

  protected BadgeRepository getBadgeRepository() {
    if (badgeRepository == null) {
      badgeRepository = new BadgeRepository();
    }
    return badgeRepository;
  }

  protected UserAnswerRepository getUserAnswerRepository() {
    if (userAnswerRepository == null) {
      userAnswerRepository = new UserAnswerRepository();
    }
    return userAnswerRepository;
  }

  protected UserReferralRepository getUserReferralRepository() {
    if (userReferralRepository == null) {
      userReferralRepository = new UserReferralRepository();
    }
    return userReferralRepository;
  }

  protected QuizPerformanceRepository getQuizPerformanceRepository() {
    if (quizPerformanceRepository == null) {
      quizPerformanceRepository = new QuizPerformanceRepository();
    }
    return quizPerformanceRepository;
  }

  protected QuizRepository getQuizRepository() {
    if (quizRepository == null) {
      quizRepository = new QuizRepository();
    }
    return quizRepository;
  }
  
  protected QuestionRepository getQuestionRepository() {
    if (questionRepository == null) {
      questionRepository = new QuestionRepository();
    }
    return questionRepository;
  }

  protected AnswerChallengeCounterService getAnswerChallengeCounterService() {
    if (answerChallengeCounterService == null) {
      answerChallengeCounterService = new AnswerChallengeCounterService(
          getAnswerChallengeCounterRepository());
    }
    return answerChallengeCounterService;
  }

  protected BadgeAssignmentService getBadgeAssignmentService() {
    if (badgeAssignmentService == null) {
      badgeAssignmentService = new BadgeAssignmentService(getBadgeAssignmentRepository());
    }
    return badgeAssignmentService;
  }

  protected BadgeService getBadgeService() {
    if (badgeService == null) {
      badgeService = new BadgeService(getBadgeRepository(), getBadgeAssignmentRepository());
    }
    return badgeService;
  }

  protected QuestionService getQuestionService() {    
    if (questionService == null) {
      questionService = new QuestionService(getQuestionRepository(), getUserAnswerRepository());
    }
    return questionService;
  }

  protected QuizPerformanceService getQuizPerformanceService() {
    if (quizPerformanceService == null) {
      quizPerformanceService = new QuizPerformanceService(getQuizPerformanceRepository());
    }
    return quizPerformanceService;
  }

  protected void initAnswerChallengeCounterService() {
    assertNotNull(getAnswerChallengeCounterService());
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID1, QUESTION_ID1));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID1, QUESTION_ID2));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID1, QUESTION_ID3));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID1, QUESTION_ID4));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID1, QUESTION_ID5));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID2, QUESTION_ID6));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID2, QUESTION_ID7));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID2, QUESTION_ID8));
    answerChallengeCounterService.save(new AnswerChallengeCounter(QUIZ_ID2, QUESTION_ID9));
  } 
  
  protected void initBadgeAssignmentService() {
    assertNotNull(getBadgeAssignmentService());
    badgeAssignmentService.save(new BadgeAssignment(USER_ID1, BADGE_NAME1));
  } 

  protected void initBadgeService() {
    assertNotNull(getBadgeService());
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

  protected void initQuestionService() {
    assertNotNull(getQuestionService());
    initUserAnswerRepository();

    // Quiz 1 has 5 questions, 2 are calibration, 3 are collections.
    // Question 1 and 4 have the same client id.
    questionService.save(
        new Question(QUIZ_ID1, "test1", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID1,
                     QUESTION_CLIENT_ID1, true  /* is Gold */, false  /* Not silver */, 1.5));
    questionService.save(
        new Question(QUIZ_ID1, "test2", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID2,
                     QUESTION_CLIENT_ID2, false, true, 0.9));
    questionService.save(
        new Question(QUIZ_ID1, "test3", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID3,
                     QUESTION_CLIENT_ID3, false, true, 0.3));
    questionService.save(
        new Question(QUIZ_ID1, "test4", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID4,
                     QUESTION_CLIENT_ID1, true, false, 1.1));
    questionService.save(
        new Question(QUIZ_ID1, "test5", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID5,
                     QUESTION_CLIENT_ID4, false, true, 0.45));

    // Quiz 2 has 4 questions, 1 is calibration, 3 are collections.
    // All the questions have null or empty client id.
    questionService.save(
        new Question(QUIZ_ID2, "test6", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID6, "",
                     true, false, 1.5));
    questionService.save(
        new Question(QUIZ_ID2, "test7", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID7, "",
                     false, true, 0.7));
    questionService.save(
        new Question(QUIZ_ID2, "test8", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID8, null,
                     false, true, 0.3));
    questionService.save(
        new Question(QUIZ_ID2, "test9", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID9, null,
                     false, true, 0.2));
  }

  protected void initQuizPerformanceService() {
    assertNotNull(getQuizPerformanceService());
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID1);
    quizPerformance.setScore(1.6);
    quizPerformance.setCorrectanswers(3);
    quizPerformance.setIncorrectanswers(1);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID2);
    quizPerformance.setScore(0.52);
    quizPerformance.setCorrectanswers(1);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID2, USER_ID1);
    quizPerformance.setScore(0.8);
    quizPerformance.setCorrectanswers(1);
    quizPerformance.setIncorrectanswers(1);
    quizPerformanceService.save(quizPerformance);
  }

  // Returns a list of Question of num * 2 size, each with numChoices answers for the given quizID.
  // The returned list will have num number of calibration questions and num number of collection
  // questions, where the calibration question has the first answer as gold.
  // The questionID will range from 1 to num for calibration questions and num + 1 to 2 * num
  // for collection questions.
  protected List<Question> getFakeMultipleChoiceQuestions(int num, int numChoices, String quizID) {
    assertTrue(numChoices > 0);
    assertTrue(num > 0);
    List<Question> questions = new ArrayList<Question>();
    for (int i = 1; i <= num; ++i) {
      Long questionID = (long) i;
      Question question = new Question(
          quizID, "Calibration Question " + i, QuestionKind.MULTIPLE_CHOICE_CALIBRATION,
          questionID, "client_gold_" + i, true  /* is Gold */, false  /* Not silver */, 0.0);
      for (int j = 0; j < numChoices; ++j) {
        question.addAnswer(new Answer(questionID, quizID, "Answer " + j,
                                      j == 0 ? AnswerKind.GOLD : AnswerKind.INCORRECT, j));
      }
      questions.add(question);
    }
    for (int i = 1; i <= num; ++i) {
      Long questionID = (long) (i + num);
      Question question = new Question(
          quizID, "Collection Question " + i, QuestionKind.MULTIPLE_CHOICE_COLLECTION,
          questionID, "client_silver_" + i, false, true, 0.0);
      for (int j = 0; j < numChoices; ++j) { 
        question.addAnswer(new Answer(questionID, quizID, "Answer " + j,
                                      AnswerKind.SILVER, j));
      } 
      questions.add(question);
    }
    return questions;
  }

  // Returns a list of UserAnswer from startNum to endNum questionID for the given userid,
  // answerID and quizID. The UserAnswer is correct if answerID == 0.
  protected List<UserAnswer> getFakeMultipleChoiceUserAnswers(
      int startNum, int endNum, String userid, String quizID, int answerID) {
    assertTrue(startNum > 0);
    assertTrue(endNum >= startNum);
    List<UserAnswer> userAnswers = new ArrayList<UserAnswer>();
    for (int i = startNum; i <= endNum; ++i) {
      UserAnswer userAnswer = new UserAnswer(userid, (long) i, answerID, quizID);
      userAnswer.setCorrect(answerID == 0);
      userAnswer.setAction("Submit");
      userAnswer.setTimestamp((long) i);
      userAnswers.add(userAnswer);
    }
    return userAnswers;
  }

  // Returns a map of (userID, QuizPerformance) of num size for the given quizID, where the userID
  // starts from 1 to num (inclusive), and score is equal to userID.
  protected Map<Integer, QuizPerformance> getFakeQuizPerformances(int num, String quizID) {
    Map<Integer, QuizPerformance> quizPerformances = new HashMap<Integer, QuizPerformance>();
    for (int userID = 1; userID <= num; ++userID) {
      QuizPerformance quizPerformance = new QuizPerformance(quizID, Integer.toString(userID));
      quizPerformance.setScore((double) userID);
      quizPerformances.put(userID, quizPerformance);
    }
    return quizPerformances;
  }
}
