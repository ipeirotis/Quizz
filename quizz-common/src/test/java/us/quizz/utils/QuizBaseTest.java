package us.quizz.utils;

import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

import org.junit.After;
import org.junit.Before;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.BadgeAssignmentRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.ExperimentRepository;
import us.quizz.repository.ExplorationExploitationResultRepository;
import us.quizz.repository.QuestionRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.SurvivalProbabilityResultRepository;
import us.quizz.repository.UserAnswerFeedbackRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.AnswerChallengeCounterService;
import us.quizz.service.AuthService;
import us.quizz.service.BadgeAssignmentService;
import us.quizz.service.BadgeService;
import us.quizz.service.BrowserStatsService;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizPerformanceService;
import us.quizz.service.QuizService;
import us.quizz.service.SurvivalProbabilityService;
import us.quizz.service.UserAnswerFeedbackService;
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
  protected static final String QUIZ_ID3 = "quizid_3";
  protected static final Long QUESTION_ID1 = 2001L;
  protected static final Long QUESTION_ID2 = 2002L;
  protected static final Long QUESTION_ID3 = 2003L;
  protected static final Long QUESTION_ID4 = 2004L;
  protected static final Long QUESTION_ID5 = 2005L;
  protected static final Long QUESTION_ID6 = 2006L;
  protected static final Long QUESTION_ID7 = 2007L;
  protected static final Long QUESTION_ID8 = 2008L;
  protected static final Long QUESTION_ID9 = 2009L;
  protected static final Long QUESTION_ID10 = 2010L;
  protected static final String QUESTION_CLIENT_ID1 = "qclient_1";
  protected static final String QUESTION_CLIENT_ID2 = "qclient_2";
  protected static final String QUESTION_CLIENT_ID3 = "qclient_3";
  protected static final String QUESTION_CLIENT_ID4 = "qclient_4";
  protected static final int ANSWER_ID0 = 0;
  protected static final int ANSWER_ID1 = 1;
  protected static final int ANSWER_ID2 = 2;
  protected static final int ANSWER_ID3 = 3;
  protected static final String BADGE_NAME1 = "5 Correct";
  protected static final String BADGE_SHORTNAME1 = "5C";
  protected static final String BROWSER_STRING = "CHROME";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(), 
          new LocalTaskQueueTestConfig(),
          new LocalMemcacheServiceTestConfig());

  protected AnswerChallengeCounterRepository answerChallengeCounterRepository = null;
  protected BadgeAssignmentRepository badgeAssignmentRepository = null;
  protected BadgeRepository badgeRepository = null;
  protected BrowserStatsRepository browserStatsRepository = null;
  protected DomainStatsRepository domainStatsRepository = null;
  protected ExperimentRepository experimentRepository = null;
  protected ExplorationExploitationResultRepository explorationExploitationResultRepository = null;
  protected QuestionRepository questionRepository = null;
  protected QuizPerformanceRepository quizPerformanceRepository = null;
  protected QuizRepository quizRepository = null;
  protected SurvivalProbabilityResultRepository survivalProbabilityResultRepository = null;
  protected UserAnswerFeedbackRepository userAnswerFeedbackRepository = null;
  protected UserAnswerRepository userAnswerRepository = null;
  protected UserReferralRepository userReferralRepository = null;
  protected UserRepository userRepository = null;

  protected AnswerChallengeCounterService answerChallengeCounterService = null;
  protected AuthService authService = null;
  protected BadgeAssignmentService badgeAssignmentService = null;
  protected BadgeService badgeService = null;
  protected BrowserStatsService browserStatsService = null;
  protected ExplorationExploitationService explorationExploitationService = null;
  protected QuestionService questionService = null;
  protected QuizPerformanceService quizPerformanceService = null;
  protected QuizService quizService = null;
  protected SurvivalProbabilityService survivalProbabilityService = null;
  protected UserAnswerFeedbackService userAnswerFeedbackService = null;
  protected UserAnswerService userAnswerService = null;
  protected UserReferralService userReferralService = null;
  protected UserService userService = null;

  @Before
  public void setUp() {
    helper.setUp();
    answerChallengeCounterRepository = null;
    badgeAssignmentRepository = null;
    badgeRepository = null;
    browserStatsRepository = null;
    domainStatsRepository = null;
    explorationExploitationResultRepository = null;
    questionRepository = null;
    quizPerformanceRepository = null;
    quizRepository = null;
    survivalProbabilityResultRepository = null;
    userAnswerFeedbackRepository = null;
    userAnswerRepository = null;
    userReferralRepository = null;

    answerChallengeCounterService = null;
    authService = null;
    badgeAssignmentService = null;
    badgeService = null;
    browserStatsService = null;
    explorationExploitationService = null;
    questionService = null;
    quizPerformanceService = null;
    quizService = null;
    survivalProbabilityService = null;
    userAnswerFeedbackService = null;
    userAnswerService = null;
    userReferralService = null;
  }

  @After
  public void tearDown() {
    if (answerChallengeCounterRepository != null) {
      answerChallengeCounterRepository.wipeClean();
    }
    if (badgeAssignmentRepository != null) {
      badgeAssignmentRepository.wipeClean();
    }
    if (badgeRepository != null) {
      badgeRepository.wipeClean();
    }
    if (browserStatsRepository != null) {
      browserStatsRepository.wipeClean();
    }
    if (domainStatsRepository != null) {
      domainStatsRepository.wipeClean();
    }
    if (explorationExploitationResultRepository != null) {
      explorationExploitationResultRepository.wipeClean();
    }
    if (questionRepository != null) {
      questionRepository.wipeClean();
    }
    if (quizPerformanceRepository != null) {
      quizPerformanceRepository.wipeClean();
    }
    if (quizRepository != null) {
      quizRepository.wipeClean();
    }
    if (survivalProbabilityResultRepository != null) {
      survivalProbabilityResultRepository.wipeClean();
    }
    if (userAnswerFeedbackRepository != null) {
      userAnswerFeedbackRepository.wipeClean();
    }
    if (userAnswerRepository != null) {
      userAnswerRepository.wipeClean();
    }
    if (userReferralRepository != null) {
      userReferralRepository.wipeClean();
    }

    helper.tearDown();
  }

  protected AnswerChallengeCounterRepository getAnswerChallengeCounterRepository() {
    if (answerChallengeCounterRepository == null) {
      answerChallengeCounterRepository = new AnswerChallengeCounterRepository();
    }
    return answerChallengeCounterRepository;
  }

  protected AuthService getAuthService() {
    if (authService == null) {
      authService = new AuthService();
    }
    return authService;
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

  protected ExplorationExploitationResultRepository getExplorationExploitationResultRepository() {
    if (explorationExploitationResultRepository == null) {
      explorationExploitationResultRepository = new ExplorationExploitationResultRepository();
    }
    return explorationExploitationResultRepository;
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

  protected UserAnswerFeedbackRepository getUserAnswerFeedbackRepository() {
    if (userAnswerFeedbackRepository == null) {
      userAnswerFeedbackRepository = new UserAnswerFeedbackRepository();
    }
    return userAnswerFeedbackRepository;
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

  protected ExplorationExploitationService getExplorationExploitationService() {
    if (explorationExploitationService == null) {
      explorationExploitationService = new ExplorationExploitationService(
          getSurvivalProbabilityService(), getExplorationExploitationResultRepository());
    }
    return explorationExploitationService;
  }

  protected QuestionService getQuestionService() {    
    if (questionService == null) {
      questionService = new QuestionService(getQuestionRepository(), getUserAnswerRepository(),
          getQuizRepository(), getUserService());
    }
    return questionService;
  }

  protected QuizPerformanceService getQuizPerformanceService() {
    if (quizPerformanceService == null) {
      quizPerformanceService = new QuizPerformanceService(
          getQuizPerformanceRepository(), getUserAnswerService(), getQuestionService());
    }
    return quizPerformanceService;
  }

  protected QuizService getQuizService() {
    if (quizService == null) {
      quizService = new QuizService(
          getUserReferralService(),
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

  protected UserAnswerFeedbackService getUserAnswerFeedbackService() {
    if (userAnswerFeedbackService == null) {
      userAnswerFeedbackService = new UserAnswerFeedbackService(getUserAnswerFeedbackRepository());
    }
    return userAnswerFeedbackService;
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

  protected void addAnswers(Question question, Long questionID, int numChoices, String quizID,
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
      Answer answer = new Answer(questionID, quizID, "Answer " + j, kind, j);
      answer.setProbCorrect(j == 0 ? 0.70 : 0.1);
      answer.setNumberOfPicks(j == 0 ? 1 : 0);
      question.addAnswer(answer);
    }
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
          quizID, new Text("Calibration Question " + i), QuestionKind.MULTIPLE_CHOICE_CALIBRATION,
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
          quizID, new Text("Collection Question " + i), QuestionKind.MULTIPLE_CHOICE_COLLECTION,
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
      userAnswer.setAction(UserAnswer.SUBMIT);
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
