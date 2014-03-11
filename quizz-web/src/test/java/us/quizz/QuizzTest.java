package us.quizz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.TreatmentRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.SurvivalProbabilityService;
import us.quizz.service.UserQuizStatisticsService;
import us.quizz.utils.PMF;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PMF.class)
@SuppressWarnings("unused")
public class QuizzTest {
	
	private static final Logger logger = Logger.getLogger(QuizzTest.class.getName());
	
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36";
	private static final String IP_ADDRESS = "192.168.0.1";
	private static final String REFERER = "http://www.google.com";
	private static final String QUIZ_ID = "testQuizId";
	private static final String USER_ID = "testUserId";
	private static final int NUMBER_OF_QUESTIONS = 10;
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(), 
			new LocalTaskQueueTestConfig().setQueueXmlPath("src/main/webapp/WEB-INF/queue.xml").setDisableAutoTaskExecution(true),
			new LocalMemcacheServiceTestConfig())
			.setEnvEmail("test@example.com")
		    .setEnvIsAdmin(true)
		    .setEnvIsLoggedIn(true);
	
	private QuizRepository quizRepository;
	private QuizQuestionRepository quizQuestionRepository;
	private AnswerChallengeCounterRepository answerChallengeCounterRepository;
	private UserAnswerRepository userAnswerRepository;
	private QuizPerformanceRepository quizPerformanceRepository;
	private BadgeRepository badgeRepository;
	private AnswersRepository answersRepository;
	private UserRepository userRepository;
	private TreatmentRepository treatmentRepository;
	private UserReferralRepository userReferralRepository;
	
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
		
		PowerMockito.mockStatic(PMF.class);    
		PowerMockito.when(PMF.getPM()).thenReturn(getPersistenceManager());
		
		helper.setUp();
		gson = new GsonBuilder().setPrettyPrinting().create();

		answerChallengeCounterRepository = new AnswerChallengeCounterRepository();
		userAnswerRepository = new UserAnswerRepository();
		quizPerformanceRepository = new QuizPerformanceRepository();
		badgeRepository = new BadgeRepository();
		userRepository = new UserRepository();
		treatmentRepository = new TreatmentRepository();
		userReferralRepository = new UserReferralRepository();
		quizRepository = new QuizRepository(userReferralRepository,	quizPerformanceRepository);
		quizQuestionRepository = new QuizQuestionRepository(quizRepository);
		answersRepository = new AnswersRepository(quizQuestionRepository);
		
		survivalProbabilityService = new SurvivalProbabilityService(quizPerformanceRepository);
		explorationExploitationService = new ExplorationExploitationService(survivalProbabilityService);
		userQuizStatisticsService = new UserQuizStatisticsService(userAnswerRepository, quizPerformanceRepository);
	
		quizEndpoint = new QuizEndpoint(quizRepository, quizQuestionRepository);
		questionEndpoint = new QuestionEndpoint(quizQuestionRepository, 
				answerChallengeCounterRepository);
		processUserAnswerEndpoint = new ProcessUserAnswerEndpoint(userRepository, 
				answersRepository, quizQuestionRepository, badgeRepository, 
				quizPerformanceRepository, userAnswerRepository, explorationExploitationService);
		treatmentEndpoint = new TreatmentEndpoint(treatmentRepository);
		userEndpoint = new UserEndpoint(userRepository, userReferralRepository);
		quizPerformanceEndpoint = new QuizPerformanceEndpoint(quizPerformanceRepository);
		
		questionsToCreate = new HashMap<String, Question>();
		
		for(int i=1; i<=NUMBER_OF_QUESTIONS; i++){
			Question question = new Question(QUIZ_ID, "Question_" + i, 1.0);
			
			for(int j=1; j<=4; j++){
				Answer answer = new Answer(null, QUIZ_ID, "Answer_" + j, j);
				answer.setKind(j==1?"selectable_gold":"selectable_not_gold");
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
	
	private PersistenceManager getPersistenceManager() {System.out.println("get pm .......");
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

	      return JDOHelper.getPersistenceManagerFactory(newProperties).getPersistenceManager();
	}

	//@Test
	public void run() throws Exception {

		//create new quiz
		Quiz quiz = createQuiz(new Quiz("testName", "testQuizId"));
			
		//create questions with answers
		for(Question question : questionsToCreate.values()){			
			createQuestion(question);
		}
		
		//listQuestions
		listQuestions(NUMBER_OF_QUESTIONS);
	
		//create treatments
		for(String treatment : treatments){
			createTreatment(treatment);
		}
		
		//update quiz questions count
		updateQuizCounts(quiz);
	
		//get user
		User user = getUser();
		
		//list quizzes
		listQuizes(1);
		
		//start quiz
		Set<Question> quizQuestions = startQuiz(quiz.getQuizID());
		
		//process user answers
		for(Question question :quizQuestions){
			processUserAnswer(user, quiz, question);
		}
		
		//get quiz performance
		getQuizPerformance(quiz.getQuizID(), user.getUserid());
	}
	
	private void logResponse(String title, Object resp){
		System.out.println("------------------" + title + "-------------------------");
		System.out.println(gson.toJson(resp));
		System.out.println("-------------------------------------------------------");
	}

	private Quiz createQuiz(Quiz quiz){
		Quiz newQuiz = quizEndpoint.insertQuiz(quiz);
		logResponse("create quiz", newQuiz);
		Assert.assertEquals(newQuiz.getQuizID(), QUIZ_ID);
		return newQuiz;
	}
	
	private void updateQuizCounts(Quiz quiz){
		quizRepository.updateQuizCounts(quiz.getQuizID());
	}
	
	private void listQuizes(int expectedListSize){
		CollectionResponse<Quiz> resp = quizEndpoint.listQuiz(null, null);
		Assert.assertEquals(resp.getItems().size(), expectedListSize);
		logResponse("list quizes", resp.getItems());
	}
	
	private Question createQuestion(Question question){
		Question newQuestion = questionEndpoint.insertQuestion(question);
		Assert.assertNotNull(newQuestion.getID());
		return newQuestion;
	}
	
	private void listQuestions(int expectedQuestionsCount){
		CollectionResponse<Question> resp = questionEndpoint.listQuestions(null);
		Assert.assertEquals(resp.getItems().size(), expectedQuestionsCount);
	}
	
	private void createTreatment(String name){
		Treatment treatment = treatmentEndpoint.addTreatment(name, 1.0);
		Assert.assertNotNull(treatment.getKey());
	}
	
	private Set<Question> startQuiz(String quizId){
		Map<String, Set<Question>> questionMap = quizEndpoint.getNextQuestions(quizId, NUMBER_OF_QUESTIONS);
		return questionMap.get("gold");
	}
	
	private void processUserAnswer(User user, Quiz quiz, Question question) throws Exception{
		HttpServletRequest req = mock(HttpServletRequest.class);
		
		when(req.getRemoteAddr()).thenReturn(IP_ADDRESS);
		when(req.getParameter("quizID")).thenReturn(QUIZ_ID);
		when(req.getHeader("User-Agent")).thenReturn(USER_AGENT);

		Map<String, Object> resp = processUserAnswerEndpoint.processUserAnswer(req, 
				quiz.getQuizID(), question.getID(), 0, user.getUserid(), 
				numberOfCorrectAnswers, NUMBER_OF_QUESTIONS, null, 
				numberOfCorrectAnswers, (NUMBER_OF_QUESTIONS - numberOfCorrectAnswers), 0);
		
		UserAnswer ua = (UserAnswer) resp.get("userAnswer");
		
		if(ua.getIsCorrect()){
			numberOfCorrectAnswers++;
		}
		
		userQuizStatisticsService.updateStatistics(quiz.getQuizID(), user.getUserid());
		
		logResponse("process user answer", resp);
	}
	
	private User getUser(){
		HttpServletRequest req = mock(HttpServletRequest.class);
		
		when(req.getRemoteAddr()).thenReturn(IP_ADDRESS);
		when(req.getParameter("quizID")).thenReturn(QUIZ_ID);
		when(req.getHeader("User-Agent")).thenReturn(USER_AGENT);
		
		Map<String, Object> map = userEndpoint.getUser(req, USER_ID);
		User user = (User)map.get("user");
		logResponse("get user", user);
		return user;
	}
	
	private void getQuizPerformance(String quizId, String userId){
		QuizPerformance qp = quizPerformanceEndpoint.getQuizPerformance(quizId, userId);
		Assert.assertEquals(qp.getScore(), Double.valueOf(20.0));
		logResponse("get quiz performance", qp);
	}
}
