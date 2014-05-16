package us.quizz;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import us.quizz.endpoints.ProcessUserAnswerEndpoint;
import us.quizz.endpoints.QuestionEndpoint;
import us.quizz.endpoints.QuizEndpoint;
import us.quizz.endpoints.QuizPerformanceEndpoint;
import us.quizz.endpoints.TreatmentEndpoint;
import us.quizz.endpoints.UserEndpoint;
import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.Treatment;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.repository.ExperimentRepository;
import us.quizz.repository.ExplorationExploitationResultRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.SurvivalProbabilityResultRepository;
import us.quizz.repository.TreatmentRepository;
import us.quizz.repository.UserAnswerFeedbackRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.DomainStatsService;
import us.quizz.service.ExperimentService;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.QuizPerformanceService;
import us.quizz.service.QuizService;
import us.quizz.service.SurvivalProbabilityService;
import us.quizz.service.TreatmentService;
import us.quizz.service.UserAnswerFeedbackService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserQuizStatisticsService;
import us.quizz.service.UserReferralService;
import us.quizz.service.UserService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unused")
public class QuizzTest {
  private static final Logger logger = Logger.getLogger(QuizzTest.class.getName());

  private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) " +
      "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36";
  private static final String IP_ADDRESS = "192.168.0.1";
  private static final String REFERER = "http://www.google.com";
  private static final String QUIZ_ID = "testQuizId";
  private static final String USER_ID = "testUserId";
  private static final int NUMBER_OF_QUESTIONS = 10;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(), 
          new LocalTaskQueueTestConfig()
              .setQueueXmlPath("src/main/webapp/WEB-INF/queue.xml")
              .setDisableAutoTaskExecution(true),
          new LocalMemcacheServiceTestConfig())
      .setEnvEmail("test@example.com")
      .setEnvIsAdmin(true)
      .setEnvIsLoggedIn(true);

  private PersistenceManager persistenceManager;
  private PersistenceManager actualPersistenceManager;

  private QuizRepository quizRepository;
  private QuizQuestionRepository quizQuestionRepository;
  private AnswerChallengeCounterRepository answerChallengeCounterRepository;
  private UserAnswerRepository userAnswerRepository;
  private UserAnswerFeedbackRepository userAnswerFeedbackRepository;
  private QuizPerformanceRepository quizPerformanceRepository;
  private BadgeRepository badgeRepository;
  private AnswersRepository answersRepository;
  private UserRepository userRepository;
  private TreatmentRepository treatmentRepository;
  private UserReferralRepository userReferralRepository;
  private DomainStatsRepository domainStatsRepository;
  private SurvivalProbabilityResultRepository survivalProbabilityResultRepository;
  private ExplorationExploitationResultRepository explorationExploitationResultRepository;
  private ExperimentRepository experimentRepository;

  private ExperimentService experimentService;
  private UserService userService;
  private QuizPerformanceService quizPerformanceService;
  private UserAnswerFeedbackService userAnswerFeedbackService;
  private UserAnswerService userAnswerService;
  private TreatmentService treatmentService;
  private DomainStatsService domainStatsService;
  private UserReferralService userReferralService;
  private QuizService quizService;
  private SurvivalProbabilityService survivalProbabilityService;
  private ExplorationExploitationService explorationExploitationService;
  private UserQuizStatisticsService userQuizStatisticsService;

  private QuizEndpoint quizEndpoint;
  private QuestionEndpoint questionEndpoint;
  private ProcessUserAnswerEndpoint processUserAnswerEndpoint;
  private TreatmentEndpoint treatmentEndpoint;
  private UserEndpoint userEndpoint;
  private QuizPerformanceEndpoint quizPerformanceEndpoint;

  private Map<String, Question> questionsToCreate;
  private List<String> treatments;
  private Gson gson;

  private int numberOfCorrectAnswers = 0;

  @Before
  public void setUp() {

    helper.setUp();
    initPersistenceManager();
    gson = new GsonBuilder().setPrettyPrinting().create();
    
    answerChallengeCounterRepository = spy(new AnswerChallengeCounterRepository());
    userAnswerRepository = spy(new UserAnswerRepository());
    userAnswerFeedbackRepository = spy(new UserAnswerFeedbackRepository());
    quizPerformanceRepository = spy(new QuizPerformanceRepository());
    badgeRepository = spy(new BadgeRepository());
    userRepository = spy(new UserRepository());
    treatmentRepository = spy(new TreatmentRepository());
    userReferralRepository = spy(new UserReferralRepository());
    quizRepository = spy(new QuizRepository());
    domainStatsRepository = spy(new DomainStatsRepository());
    quizQuestionRepository = spy(new QuizQuestionRepository(quizRepository, userAnswerRepository));
    answersRepository = spy(new AnswersRepository(quizQuestionRepository));
    survivalProbabilityResultRepository = spy(new SurvivalProbabilityResultRepository());
    explorationExploitationResultRepository = spy(new ExplorationExploitationResultRepository());
    experimentRepository = spy(new ExperimentRepository());

    when(answerChallengeCounterRepository.getPersistenceManager()).thenReturn(getPersistenceManager());
    when(quizQuestionRepository.getPersistenceManager()).thenReturn(getPersistenceManager());
    when(answersRepository.getPersistenceManager()).thenReturn(getPersistenceManager());

    treatmentService = new TreatmentService(treatmentRepository);
    experimentService = new ExperimentService(experimentRepository, treatmentService);
    userService = new UserService(userRepository, experimentService);
    quizPerformanceService = new QuizPerformanceService(quizPerformanceRepository);
    userAnswerService = new UserAnswerService(userAnswerRepository);
    domainStatsService = new DomainStatsService(domainStatsRepository);
    userReferralService = new UserReferralService(userReferralRepository, domainStatsRepository);
    quizService = new QuizService(userReferralService, quizPerformanceService, quizRepository, 
        quizQuestionRepository, userAnswerService);
    survivalProbabilityService = new SurvivalProbabilityService(quizPerformanceService,
        survivalProbabilityResultRepository);
    explorationExploitationService = new ExplorationExploitationService(survivalProbabilityService,
        explorationExploitationResultRepository);
    userQuizStatisticsService = new UserQuizStatisticsService(
        userAnswerService, quizPerformanceService, quizQuestionRepository);
    userAnswerFeedbackService = new UserAnswerFeedbackService(userAnswerFeedbackRepository);

    quizEndpoint = new QuizEndpoint(quizService, quizQuestionRepository);
    questionEndpoint = new QuestionEndpoint(quizService, quizQuestionRepository,
        answerChallengeCounterRepository);
    processUserAnswerEndpoint = new ProcessUserAnswerEndpoint(quizService, userService,
        answersRepository, quizQuestionRepository, badgeRepository, quizPerformanceService,
        userAnswerService, userAnswerFeedbackService, explorationExploitationService);
    treatmentEndpoint = new TreatmentEndpoint(treatmentService);
    userEndpoint = new UserEndpoint(userService, userReferralService, experimentService);
    quizPerformanceEndpoint = new QuizPerformanceEndpoint(quizPerformanceService);

    questionsToCreate = new HashMap<String, Question>();

    for (int i = 1; i <= NUMBER_OF_QUESTIONS; i++) {
      Question question = new Question(QUIZ_ID, "Question_" + i, QuestionKind.MULTIPLE_CHOICE_CALIBRATION);
      for (int j = 1; j <= 4; j++) {
        AnswerKind ak = (j == 1)? AnswerKind.GOLD : AnswerKind.INCORRECT;
        Answer answer = new Answer(null, QUIZ_ID, "Answer_" + j, ak, j);
        answer.setQuestionID(1L);
        question.addAnswer(answer);
      }
      questionsToCreate.put("question_" + i, question);
    }

    treatments = Arrays.asList("Correct", "CrowdAnswers", "Difficulty", "Message",
        "PercentageCorrect", "percentageRank", "Score", "TotalCorrect", "TotalCorrectRank");
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private void initPersistenceManager() {
    Properties newProperties = new Properties();
    newProperties
    .put("javax.jdo.PersistenceManagerFactoryClass",
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

  private PersistenceManager getPersistenceManager(){
    return persistenceManager;
  } 

  @Test
  public void run() throws Exception {
    // create new quiz
    Quiz quiz = createQuiz(new Quiz("testName", "testQuizId", QuizKind.MULTIPLE_CHOICE));

    // create questions with answers
    for (Question question : questionsToCreate.values()) {
      createQuestion(question);
    }

    // listQuestions
    listQuestions(NUMBER_OF_QUESTIONS);
    
    //add FREE_TEXT question to MULTIPLE_CHOICE quiz.
    //should throw an exception BadRequestException
    createFreeTextQuestionInMultichoiceQuiz(
        new Question(QUIZ_ID, "Question", QuestionKind.FREETEXT_CALIBRATION));

    // create treatments
    for (String treatment : treatments) {
      createTreatment(treatment);
    }

    // update quiz questions count
    updateQuizCounts(quiz, 10);

    // get user
    User user = getUser();

    // list quizzes
    listQuizes(1);

    // start quiz
    Set<Question> quizQuestions = startQuiz(quiz.getQuizID());

    // process user answers
    int index = 1;
    for (Question question : quizQuestions) {
      processUserAnswer(user, quiz, question, index++);
    }

    // get quiz performance
    getQuizPerformance(quiz.getQuizID(), user.getUserid(), 20.0);
  }

  private void logResponse(String title, Object resp) {
    System.out.println("------------------" + title + "-------------------------");
    System.out.println(gson.toJson(resp));
    System.out.println("-------------------------------------------------------");
  }

  private Quiz createQuiz(Quiz quiz) {
    Quiz newQuiz = quizEndpoint.insertQuiz(quiz);
    logResponse("create quiz", newQuiz);
    Assert.assertEquals(newQuiz.getQuizID(), QUIZ_ID);
    return newQuiz;
  }

  private void updateQuizCounts(Quiz quiz, Integer expectedGoldCount) {
    Quiz updatedQuiz = quizService.updateQuizCounts(quiz.getQuizID());
    Assert.assertEquals(updatedQuiz.getGold(), expectedGoldCount);
    logResponse("update quiz counts", updatedQuiz);
  }

  private void listQuizes(int expectedListSize) {
    CollectionResponse<Quiz> resp = quizEndpoint.listQuiz(null, null);
    Assert.assertEquals(resp.getItems().size(), expectedListSize);
    logResponse("list quizes", resp.getItems());
  }

  private Question createQuestion(Question question) throws BadRequestException {
    Question newQuestion =  questionEndpoint.insertQuestion(question);
    Assert.assertNotNull(newQuestion.getID());
    return newQuestion;
  }
  
  private void createFreeTextQuestionInMultichoiceQuiz(Question question) {
    try {
      questionEndpoint.insertQuestion(question);
      Assert.fail("this method should throw an exception BadRequestException");
    } catch (BadRequestException e) {
      //success
    }
  }

  private void listQuestions(int expectedQuestionsCount) {
    CollectionResponse<Question> resp = questionEndpoint.listQuestions(null);
    Assert.assertEquals(resp.getItems().size(), expectedQuestionsCount);
  }

  private void createTreatment(String name) {
    Treatment treatment = treatmentEndpoint.addTreatment(name, 1.0);
    Assert.assertNotNull(treatment.getName());
  }

  private Set<Question> startQuiz(String quizId) {
    Map<String, Set<Question>> questionMap =
        quizEndpoint.getNextQuestions(quizId, "user1", NUMBER_OF_QUESTIONS);
    return questionMap.get("calibration");
  }

  private void processUserAnswer(User user, Quiz quiz, Question question, 
      Integer expectedNumOfCorrectAnswers) throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);

    when(req.getRemoteAddr()).thenReturn(IP_ADDRESS);
    when(req.getParameter("quizID")).thenReturn(QUIZ_ID);
    when(req.getHeader("User-Agent")).thenReturn(USER_AGENT);

    Map<String, Object> resp = processUserAnswerEndpoint.processUserAnswer(req,
        quiz.getQuizID(), question.getID(), 0, user.getUserid(),
        numberOfCorrectAnswers, NUMBER_OF_QUESTIONS, null,
        numberOfCorrectAnswers, (NUMBER_OF_QUESTIONS - numberOfCorrectAnswers), 0);

    UserAnswerFeedback userAnswerFeedback = (UserAnswerFeedback) resp.get("userAnswerFeedback");
    Assert.assertEquals(userAnswerFeedback.getNumCorrectAnswers(), expectedNumOfCorrectAnswers);
    
    UserAnswer ua = (UserAnswer) resp.get("userAnswer");

    if (ua.getIsCorrect()) {
      numberOfCorrectAnswers++;
    }

    userQuizStatisticsService.updateStatistics(quiz.getQuizID(), user.getUserid());

    logResponse("process user answer", resp);
  }

  private User getUser() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRemoteAddr()).thenReturn(IP_ADDRESS);
    when(req.getParameter("quizID")).thenReturn(QUIZ_ID);
    when(req.getHeader("User-Agent")).thenReturn(USER_AGENT);

    Map<String, Object> map = userEndpoint.getUser(req, USER_ID);
    User user = (User)map.get("user");
    logResponse("get user", user);
    return user;
  }

  private void getQuizPerformance(String quizId, String userId, Double expectedScore) {
    QuizPerformance qp = quizPerformanceEndpoint.getQuizPerformance(quizId, userId);
    Assert.assertEquals(qp.getScore(), expectedScore);
    logResponse("get quiz performance", qp);
  }
}
