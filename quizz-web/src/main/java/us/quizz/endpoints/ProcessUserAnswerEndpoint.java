package us.quizz.endpoints;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import us.quizz.entities.Answer;
import us.quizz.entities.Badge;
import us.quizz.entities.Question;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.repository.AnswersRepository;
import us.quizz.repository.BadgeRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserRepository;
import us.quizz.service.ExplorationExploitationService;
import us.quizz.service.ExplorationExploitationService.Result;
import us.quizz.utils.LevenshteinAlgorithm;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.common.base.Strings;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class ProcessUserAnswerEndpoint {
	
	private static final Logger logger = Logger.getLogger(ProcessUserAnswerEndpoint.class.getName());
	
	private UserRepository userRepository;
	private AnswersRepository answersRepository;
	private QuizQuestionRepository quizQuestionRepository;
	private BadgeRepository badgeRepository;
	private QuizPerformanceRepository quizPerformanceRepository;
	private UserAnswerRepository userAnswerRepository;
	private ExplorationExploitationService explorationExploitationService;
	
	@Inject
	public ProcessUserAnswerEndpoint(UserRepository userRepository,	
			AnswersRepository answersRepository,
			QuizQuestionRepository quizQuestionRepository,	
			BadgeRepository badgeRepository,
			QuizPerformanceRepository quizPerformanceRepository, 
			UserAnswerRepository userAnswerRepository,
			ExplorationExploitationService explorationExploitationService){
		this.userRepository = userRepository;
		this.answersRepository = answersRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.badgeRepository = badgeRepository;
		this.quizPerformanceRepository = quizPerformanceRepository;
		this.userAnswerRepository = userAnswerRepository;
		this.explorationExploitationService = explorationExploitationService;
	}

	@ApiMethod(name = "processUserAnswer", path="processUserAnswer", httpMethod=HttpMethod.POST)
	public Map<String, Object> processUserAnswer(HttpServletRequest req,
							@Named("quizID") String quizID, 
							@Named("questionID") Long questionID,
							@Named("answerID") Integer answerID,
							@Named("userID") String userID,
							@Named("correctanswers") Integer correctanswers,
							@Named("totalanswers") Integer totalanswers,
							@Named("userInput") String userInput,
							@Named("a") Integer a,
							@Named("b") Integer b,
							@Named("c") Integer c) throws Exception {

		User user = userRepository.get(userID);

		String action;

		List<Answer> answers = null;
		Boolean isCorrect = false;
		if (answerID != -1) {
			action = "Submit";
			Answer answer = answersRepository.getAnswer(questionID,
					answerID);
			if (answer.getKind().equals("input_text")) { // check in all
															// possible answers
				Question question = quizQuestionRepository
						.getQuizQuestion(questionID);
				answers = question.getAnswers();

				// first check for correct answers
				for (Answer ans : answers) {
					isCorrect = ans.checkIfCorrect(userInput);
					if (isCorrect) {
						userInput = userInput + " is Correct!";
						break;
					}
				}

				// if no correct answer then calculate edit distance
				if (!isCorrect) {
					for (Answer ans : answers) {
						// Checking Edit_Distance for input_text
						int editDistance = LevenshteinAlgorithm
								.getLevenshteinDistance(userInput,
										ans.getText());
						if (editDistance == 1) {
							isCorrect = true;
							userInput = userInput + " is Almost Correct!";
							break;
						}
					}
				}
			} else {
				isCorrect = answer.checkIfCorrect(userInput);
				if (isCorrect) {
					userInput = userInput + " is Correct!";
				}
			}
		} else {
			action = "I don't know";
		}

		if (isCorrect) {
			correctanswers += 1;
		}

		totalanswers += 1;
		String numCorrectAnswers = Integer.toString(correctanswers);
		String numTotalAnswers = Integer.toString(totalanswers);

		List<Badge> newBadges = badgeRepository.checkForNewBadges(user, quizID,
				numCorrectAnswers, numTotalAnswers);

		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		if (referer == null)
			referer = "";
		Long timestamp = (new Date()).getTime();

		UserAnswerFeedback uaf = createUserAnswerFeedback(user, questionID,
				answerID, userInput, isCorrect, numCorrectAnswers,
				numTotalAnswers, newBadges);
		quickUpdateQuizPerformance(user, quizID, isCorrect, action);
		UserAnswer ua = storeUserAnswer(user, quizID, questionID, action, answerID,
				userInput, ipAddress, browser, referer, timestamp, isCorrect);
		updateQuizPerformance(user, questionID);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("userAnswer", ua);
		result.put("userAnswerFeedback", uaf);
		result.put("exploit", isExploit(a, b, c));
		
		return result;
	}
	
	private boolean isExploit(int a, int b, int c) throws Exception{
		//TODO: enable code
		//explorationExploitationService.setN(10);
		//Result r = explorationExploitationService.getUtility(a, b, c, 0.01);
		//return r.getAction();
		return true;
	}

	protected UserAnswerFeedback createUserAnswerFeedback(User user,
			Long questionID, Integer useranswerID, String userInput,
			Boolean isCorrect, String numCorrectAnswers,
			String numTotalAnswers, List<Badge> newBadges) {
		UserAnswerFeedback uaf = new UserAnswerFeedback(questionID,
				user.getUserid(), useranswerID, isCorrect);
		if (!Strings.isNullOrEmpty(numCorrectAnswers))
			uaf.setNumCorrectAnswers(Integer.parseInt(numCorrectAnswers));
		if (!Strings.isNullOrEmpty(numTotalAnswers))
			uaf.setNumTotalAnswers(Integer.parseInt(numTotalAnswers));
		Question question = quizQuestionRepository.getQuizQuestion(questionID);
		uaf.setUserNewBadges(newBadges);
		uaf.setUserAnswerText((useranswerID == -1) ? "" : question.getAnswer(
				useranswerID).userAnswerText(userInput));
		uaf.setCorrectAnswerText(question.goldAnswer().getText());
		uaf.computeDifficulty();
		userAnswerRepository.storeUserAnswerFeedback(uaf);
		return uaf;
	}

	private void updateQuizPerformance(User user, Long questionID) {
		Queue queueUserStats = QueueFactory.getQueue("updateUserStatistics");
		String quizID = quizQuestionRepository.getQuizQuestion(questionID)
				.getQuizID();
		queueUserStats
				.add(Builder.withUrl("/api/updateUserQuizStatistics")
						.param("quizID", quizID)
						.param("userid", user.getUserid())
						.param("channelNotify", "true")
						.method(TaskOptions.Method.POST));
	}

	/**
	 * @param user
	 * @param quizID
	 * @param mid
	 * @param action
	 * @param useranswer
	 * @param ipAddress
	 * @param browser
	 * @param referer
	 * @param timestamp
	 * @param isCorrect
	 */
	private UserAnswer storeUserAnswer(User user, String quizID, Long questionID,
			String action, Integer useranswerID, String userInput,
			String ipAddress, String browser, String referer, Long timestamp,
			Boolean isCorrect) {

		UserAnswer ue = new UserAnswer(user.getUserid(), questionID,
				useranswerID);
		ue.setReferer(referer);
		ue.setBrowser(browser);
		ue.setIpaddress(ipAddress);
		ue.setTimestamp(timestamp);
		ue.setAction(action);
		ue.setIsCorrect(isCorrect);
		ue.setQuizID(quizID);
		ue.setUserInput(userInput);
		return userAnswerRepository.singleMakePersistent(ue);
	}

	/**
	 * With this call, we just update the counts of correct and incorrect
	 * answers. The full update happens asynchronously from the
	 * updateUserStatistics call that is placed in the task queue.
	 * 
	 * @param user
	 * @param quizID
	 * @param isCorrect
	 * @param action
	 */
	private void quickUpdateQuizPerformance(User user, String quizID,
			Boolean isCorrect, String action) {

		QuizPerformance qp = quizPerformanceRepository.getQuizPerformance(
				quizID, user.getUserid());
		if (qp == null) {
			qp = new QuizPerformance(quizID, user.getUserid());
		}

		if (isCorrect) {
			qp.increaseCorrect();
		}else{
			qp.increaseIncorrect();
		}
		
		if (action.equals("Submit")) {
			qp.increaseTotal();
		}
		
		quizPerformanceRepository.cacheQuizPerformance(qp);

	}


}