package us.quizz.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import nl.bitwalker.useragentutils.Browser;

import org.junit.After;
import org.junit.Before;

import us.quizz.entities.Answer;
import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserReferal;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeAssignmentRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.ExperimentRepository;
import us.quizz.repository.QuestionRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.SurvivalProbabilityResultRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.AnswerChallengeCounterService;
import us.quizz.service.AnswerService;
import us.quizz.service.BadgeAssignmentService;
import us.quizz.service.BadgeService;
import us.quizz.service.BrowserStatsService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizPerformanceService;
import us.quizz.service.QuizService;
import us.quizz.service.SurvivalProbabilityService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserReferralService;
import us.quizz.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// A test util class to do all the common initialization of persistent manager and repositories
// construction and content.
public class QuizBaseTest {
  protected static final String USER_ID1 = "1001";
  protected static final String USER_ID2 = "1002";
  protected static final String USER_ID3 = "1003";
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
  protected static final int ANSWER_ID0 = 0;
  protected static final int ANSWER_ID1 = 1;
  protected static final int ANSWER_ID2 = 2;
  protected static final String BADGE_NAME1 = "5 Correct";
  protected static final String BADGE_SHORTNAME1 = "5C";
  protected static final String BROWSER_STRING = "CHROME";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(), 
          new LocalTaskQueueTestConfig(),
          new LocalMemcacheServiceTestConfig());

  protected AnswerChallengeCounterRepository answerChallengeCounterRepository = null;
  protected AnswersRepository answerRepository = null;
  protected BadgeAssignmentRepository badgeAssignmentRepository = null;
  protected BadgeRepository badgeRepository = null;
  protected BrowserStatsRepository browserStatsRepository = null;
  protected DomainStatsRepository domainStatsRepository = null;
  protected ExperimentRepository experimentRepository = null;
  protected QuestionRepository questionRepository = null;
  protected QuizPerformanceRepository quizPerformanceRepository = null;
  protected QuizRepository quizRepository = null;
  protected SurvivalProbabilityResultRepository survivalProbabilityResultRepository = null;
  protected UserAnswerRepository userAnswerRepository = null;
  protected UserReferralRepository userReferralRepository = null;
  protected UserRepository userRepository = null;

  protected AnswerChallengeCounterService answerChallengeCounterService = null;
  protected AnswerService answerService = null;
  protected BadgeAssignmentService badgeAssignmentService = null;
  protected BadgeService badgeService = null;
  protected BrowserStatsService browserStatsService = null;
  protected QuestionService questionService = null;
  protected QuizPerformanceService quizPerformanceService = null;
  protected QuizService quizService = null;
  protected SurvivalProbabilityService survivalProbabilityService = null;
  protected UserAnswerService userAnswerService = null;
  protected UserReferralService userReferralService = null;
  protected UserService userService = null;

  private boolean isInitAnswerChallengeCounterService = false;
  private boolean isInitAnswerService = false;
  private boolean isInitBadgeAssignmentService = false;
  private boolean isInitBadgeService = false;
  private boolean isInitBrowserStatsService = false;
  private boolean isInitQuestionService = false;
  private boolean isInitQuizPerformanceService = false;
  private boolean isInitQuizService = false;
  private boolean isInitSurvivalProbabilityService = false;
  private boolean isInitUserAnswerService = false;
  private boolean isInitUserReferralService = false;
  private boolean isInitUserService = false;

  @Before
  public void setUp() {
    helper.setUp();
    answerChallengeCounterRepository = null;
    answerRepository = null;
    badgeAssignmentRepository = null;
    badgeRepository = null;
    browserStatsRepository = null;
    domainStatsRepository = null;
    questionRepository = null;
    quizPerformanceRepository = null;
    quizRepository = null;
    survivalProbabilityResultRepository = null;
    userAnswerRepository = null;
    userReferralRepository = null;

    answerChallengeCounterService = null;
    answerService = null;
    badgeAssignmentService = null;
    badgeService = null;
    browserStatsService = null;
    questionService = null;
    quizPerformanceService = null;
    quizService = null;
    survivalProbabilityService = null;
    userAnswerService = null;
    userReferralService = null;

    isInitAnswerChallengeCounterService = false;
    isInitAnswerService = false;
    isInitBadgeAssignmentService = false;
    isInitBadgeService = false;
    isInitBrowserStatsService = false;
    isInitQuestionService = false;
    isInitQuizPerformanceService = false;
    isInitQuizService = false;
    isInitSurvivalProbabilityService = false;
    isInitUserAnswerService = false;
    isInitUserReferralService = false;
    isInitUserService = false;
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

  protected AnswersRepository getAnswerRepository() {
    if (answerRepository == null) {
      answerRepository = new AnswersRepository();
    }
    return answerRepository;
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

  protected BrowserStatsRepository getBrowserStatsRepository() {
    if (browserStatsRepository == null) {
      browserStatsRepository = new BrowserStatsRepository();
    }
    return browserStatsRepository;
  }

  protected DomainStatsRepository getDomainStatsRepository() {
    if (domainStatsRepository == null) {
      domainStatsRepository = new DomainStatsRepository();
    }
    return domainStatsRepository;
  }

  protected ExperimentRepository getExperimentRepository() {
    if (experimentRepository == null) {
      experimentRepository = new ExperimentRepository();
    }
    return experimentRepository;
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

  protected SurvivalProbabilityResultRepository getSurvivalProbabilityResultRepository() {
    if (survivalProbabilityResultRepository == null) {
      survivalProbabilityResultRepository = new SurvivalProbabilityResultRepository();
    }
    return survivalProbabilityResultRepository;
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

  protected UserRepository getUserRepository() {
    if (userRepository == null) {
      userRepository = new UserRepository();
    }
    return userRepository;
  }

  protected AnswerChallengeCounterService getAnswerChallengeCounterService() {
    if (answerChallengeCounterService == null) {
      answerChallengeCounterService = new AnswerChallengeCounterService(
          getAnswerChallengeCounterRepository());
    }
    return answerChallengeCounterService;
  }

  protected AnswerService getAnswerService() {
    if (answerService == null) {
      answerService = new AnswerService(getAnswerRepository());
    }
    return answerService;
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

  protected BrowserStatsService getBrowserStatsService() {
    if (browserStatsService == null) {
      browserStatsService = new BrowserStatsService(
          getQuizPerformanceService(),
          getUserReferralService(),
          getBrowserStatsRepository());
    }
    return browserStatsService;
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

  protected QuizService getQuizService() {
    if (quizService == null) {
      quizService = new QuizService(
          getUserReferralService(),
          getAnswerRepository(),
          getQuizPerformanceService(),
          getQuizRepository(),
          getQuestionService(),
          getUserAnswerService());
    }
    return quizService;
  }

  protected SurvivalProbabilityService getSurvivalProbabilityService() {
    if (survivalProbabilityService == null) {
      survivalProbabilityService = new SurvivalProbabilityService(
          getQuizPerformanceService(),
          getSurvivalProbabilityResultRepository());
    }
    return survivalProbabilityService;
  }

  protected UserAnswerService getUserAnswerService() {
    if (userAnswerService == null) {
      userAnswerService = new UserAnswerService(getUserAnswerRepository());
    }
    return userAnswerService;
  }

  protected UserReferralService getUserReferralService() {
    if (userReferralService == null) {
      userReferralService = new UserReferralService(
          getUserReferralRepository(), getDomainStatsRepository());
    }
    return userReferralService;
  }

  protected UserService getUserService() {
    if (userService == null) {
      userService = new UserService(getUserRepository(), getExperimentRepository());
    }
    return userService;
  }

  protected void initAnswerChallengeCounterService() {
    if (isInitAnswerChallengeCounterService) {
      return;
    }
    isInitAnswerChallengeCounterService = true;
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

  protected void initAnswerService() {
    if (isInitAnswerService) {
      return;
    }
    isInitAnswerService = true;
    assertNotNull(getAnswerService());
  }

  protected void initBadgeAssignmentService() {
    if (isInitBadgeAssignmentService) {
      return;
    }
    isInitBadgeAssignmentService = true;
    assertNotNull(getBadgeAssignmentService());
    badgeAssignmentService.save(new BadgeAssignment(USER_ID1, BADGE_NAME1));
  } 

  protected void initBadgeService() {
    if (isInitBadgeService) {
      return;
    }
    isInitBadgeService = true;
    assertNotNull(getBadgeService());
  }

  protected void initBrowserStatsService() {
    if (isInitBrowserStatsService) {
      return;
    }
    isInitBrowserStatsService = true;
    assertNotNull(getBrowserStatsService());
    initUserReferralService();
    initQuizPerformanceService();
  }

  protected void initUserAnswerService() {
    if (isInitUserAnswerService) {
      return;
    }
    isInitUserAnswerService = true;
    assertNotNull(getUserAnswerService());

    // User 1 answers 3 questions from quiz 1, 2 of which are duplicate questions.
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID2, ANSWER_ID0, QUIZ_ID1, true));
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID1, ANSWER_ID1, QUIZ_ID1, false));
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true));
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID3, ANSWER_ID0, QUIZ_ID1, true));
    // User 1 also answers 2 questions from quiz 2.
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID6, ANSWER_ID0, QUIZ_ID2, true));
    userAnswerRepository.save(new UserAnswer(USER_ID1, QUESTION_ID9, ANSWER_ID2, QUIZ_ID2, false));

    // User 2 answers 1 question from quiz 1.
    userAnswerRepository.save(new UserAnswer(USER_ID2, QUESTION_ID1, ANSWER_ID0, QUIZ_ID1, true));

    // User 3 answers 0 questions.
  }

  private void addAnswers(Question question, Long questionID, int numChoices, String quizID,
      boolean isGold) {
    for (int j = 0; j < numChoices; ++j) {
      AnswerKind kind = AnswerKind.SILVER;
      if (isGold) {
        if (j == 0) {
          kind = AnswerKind.GOLD;
        } else {
          kind = AnswerKind.INCORRECT;
        }
      }
      question.addAnswer(new Answer(questionID, quizID, "Answer " + j, kind, j));
    }
  }

  protected void initQuestionService() {
    if (isInitQuestionService) {
      return;
    }
    isInitQuestionService = true;
    assertNotNull(getQuestionService());
    initUserAnswerService();

    // Quiz 1 has 5 questions, 2 are calibration, 3 are collections.
    // Question 1 and 4 have the same client id.
    Question question =
        new Question(QUIZ_ID1, "test1", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID1,
                     QUESTION_CLIENT_ID1, true  /* is Gold */, false  /* Not silver */, 1.5);
    addAnswers(question, QUESTION_ID1, 4, QUIZ_ID1, true);
    questionService.save(question);

    question =
        new Question(QUIZ_ID1, "test2", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID2,
                     QUESTION_CLIENT_ID2, false, true, 0.9);
    addAnswers(question, QUESTION_ID2, 4, QUIZ_ID1, false);
    questionService.save(question);

    question =
        new Question(QUIZ_ID1, "test3", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID3,
                     QUESTION_CLIENT_ID3, false, true, 0.3);
    addAnswers(question, QUESTION_ID3, 4, QUIZ_ID1, false);
    questionService.save(question);

    question =
        new Question(QUIZ_ID1, "test4", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID4,
                     QUESTION_CLIENT_ID1, true, false, 1.1);
    addAnswers(question, QUESTION_ID4, 4, QUIZ_ID1, true);
    questionService.save(question);

    question =
        new Question(QUIZ_ID1, "test5", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID5,
                     QUESTION_CLIENT_ID4, false, true, 0.45);
    addAnswers(question, QUESTION_ID5, 4, QUIZ_ID1, false);
    questionService.save(question);

    // Quiz 2 has 4 questions, 1 is calibration, 3 are collections.
    // All the questions have null or empty client id.
    question =
        new Question(QUIZ_ID2, "test6", QuestionKind.MULTIPLE_CHOICE_CALIBRATION, QUESTION_ID6, "",
                     true, false, 1.5);
    addAnswers(question, QUESTION_ID6, 4, QUIZ_ID2, true);
    questionService.save(question);

    question =
        new Question(QUIZ_ID2, "test7", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID7, "",
                     false, true, 0.7);
    addAnswers(question, QUESTION_ID7, 4, QUIZ_ID2, false);
    questionService.save(question);

    question =
        new Question(QUIZ_ID2, "test8", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID8, null,
                     false, true, 0.3);
    addAnswers(question, QUESTION_ID8, 4, QUIZ_ID2, false);
    questionService.save(question);

    question =
        new Question(QUIZ_ID2, "test9", QuestionKind.MULTIPLE_CHOICE_COLLECTION, QUESTION_ID9, null,
                     false, true, 0.2);
    addAnswers(question, QUESTION_ID9, 4, QUIZ_ID2, false);
    questionService.save(question);
  }

  protected void initQuizPerformanceService() {
    if (isInitQuizPerformanceService) {
      return;
    }
    isInitQuizPerformanceService = true;
    assertNotNull(getQuizPerformanceService());
    QuizPerformance quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID1);
    quizPerformance.setScore(1.6);
    quizPerformance.setCorrectanswers(3);
    quizPerformance.setIncorrectanswers(1);
    quizPerformance.setTotalanswers(4);
    quizPerformance.setTotalCalibrationAnswers(2);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID1, USER_ID2);
    quizPerformance.setScore(0.52);
    quizPerformance.setCorrectanswers(1);
    quizPerformance.setTotalanswers(1);
    quizPerformance.setTotalCalibrationAnswers(1);
    quizPerformanceService.save(quizPerformance);

    quizPerformance = new QuizPerformance(QUIZ_ID2, USER_ID1);
    quizPerformance.setScore(0.8);
    quizPerformance.setCorrectanswers(1);
    quizPerformance.setIncorrectanswers(1);
    quizPerformance.setTotalanswers(2);
    quizPerformance.setTotalCalibrationAnswers(2);
    quizPerformanceService.save(quizPerformance);
  }

  protected void initQuizService() {
    if (isInitQuizService) {
      return;
    }
    isInitQuizService = true;
    assertNotNull(getQuizService());
    initAnswerService();
    initQuestionService();
    initQuizPerformanceService();
    initUserAnswerService();
    initUserReferralService();

    quizService.save(new Quiz("Quiz 1", QUIZ_ID1, QuizKind.MULTIPLE_CHOICE));
    quizService.save(new Quiz("Quiz 2", QUIZ_ID2, QuizKind.MULTIPLE_CHOICE));
  }

  protected void initSurvivalProbabilityService() {
    if (isInitSurvivalProbabilityService) {
      return;
    }
    isInitSurvivalProbabilityService = true;
    assertNotNull(getSurvivalProbabilityService());    

    initQuizPerformanceService();
    survivalProbabilityService.cacheValuesInMemcache(QUIZ_ID1);
    survivalProbabilityService.saveValuesInDatastore(QUIZ_ID1);
    assertEquals(10, survivalProbabilityService.listAll().size());
  }

  protected void initUserReferralService() {
    if (isInitUserReferralService) {
      return;
    }
    isInitUserReferralService = true;

    assertNotNull(getUserReferralService());
    Browser browser = Browser.valueOf(BROWSER_STRING);
    UserReferal userReferal = new UserReferal(USER_ID1);
    userReferal.setQuiz(QUIZ_ID1);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID1);
    userReferal.setQuiz(QUIZ_ID2);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID2);
    userReferal.setQuiz(QUIZ_ID1);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);

    userReferal = new UserReferal(USER_ID3);
    userReferal.setQuiz(QUIZ_ID1);
    userReferal.setBrowser(browser);
    userReferralService.save(userReferal);
  }

  protected void initUserService() {
    if (isInitUserService) {
      return;
    }
    isInitUserService = true;

    assertNotNull(getUserService());
    userService.save(new User(USER_ID1));
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
