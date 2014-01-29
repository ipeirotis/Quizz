package us.quizz.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import us.quizz.scoring.ExplorationExploitation;
import us.quizz.scoring.ExplorationExploitation.Result;
import us.quizz.utils.LevenshteinAlgorithm;
import us.quizz.utils.ServletUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class ProcessUserAnswer extends HttpServlet {
	
	private UserRepository userRepository;
	private AnswersRepository answersRepository;
	private QuizQuestionRepository quizQuestionRepository;
	private BadgeRepository badgeRepository;
	private QuizPerformanceRepository quizPerformanceRepository;
	private UserAnswerRepository userAnswerRepository;
	
	@Inject
	public ProcessUserAnswer(UserRepository userRepository,	AnswersRepository answersRepository,
			QuizQuestionRepository quizQuestionRepository,	BadgeRepository badgeRepository,
			QuizPerformanceRepository quizPerformanceRepository, 
			UserAnswerRepository userAnswerRepository){
		this.userRepository = userRepository;
		this.answersRepository = answersRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.badgeRepository = badgeRepository;
		this.quizPerformanceRepository = quizPerformanceRepository;
		this.userAnswerRepository = userAnswerRepository;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		ServletUtils.ensureParameters(req, "quizID", "questionID", "answerID",
				"correctanswers", "totalanswers", "userInput");

		User user = userRepository.getUseridFromCookie(req, resp);
		Long questionID = Long.parseLong(req.getParameter("questionID"));
		String quizID = req.getParameter("quizID");
		String action;
		Integer useranswerID = Integer.parseInt(req.getParameter("answerID"));
		String userInput = req.getParameter("userInput");

		List<Answer> answers = null;
		Boolean isCorrect = false;
		if (useranswerID != -1) {
			action = "Submit";
			Answer answer = answersRepository.getAnswer(questionID,
					useranswerID);
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
		Integer correctAnswers = Integer.parseInt(req
				.getParameter("correctanswers"));
		if (isCorrect) {
			correctAnswers += 1;
		}
		Integer totalAnswers = Integer.parseInt(req
				.getParameter("totalanswers"));
		totalAnswers += 1;
		String numCorrectAnswers = Integer.toString(correctAnswers);
		String numTotalAnswers = Integer.toString(totalAnswers);

		List<Badge> newBadges = badgeRepository.checkForNewBadges(user, quizID,
				numCorrectAnswers, numTotalAnswers);

		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		if (referer == null)
			referer = "";
		Long timestamp = (new Date()).getTime();

		UserAnswerFeedback uaf = createUserAnswerFeedback(user, questionID,
				useranswerID, userInput, isCorrect, numCorrectAnswers,
				numTotalAnswers, newBadges);
		quickUpdateQuizPerformance(user, quizID, isCorrect, action);
		UserAnswer ua = storeUserAnswer(user, quizID, questionID, action, useranswerID,
				userInput, ipAddress, browser, referer, timestamp, isCorrect);
		updateQuizPerformance(user, questionID);
		
		returnUserAnswerFeedback(ua, uaf, isExploit(req), resp);
	}
	
	private boolean isExploit(HttpServletRequest req){
		Integer a = Integer.parseInt(req.getParameter("a"));
		Integer b = Integer.parseInt(req.getParameter("b"));
		Integer c = Integer.parseInt(req.getParameter("c"));
		
		try {
			ExplorationExploitation ex = new ExplorationExploitation(10);

			Result r = ex.getUtility(a, b, c, 0.01);
			return r.getAction();
		} catch (Exception e) {
			//TODO
			throw new RuntimeException();
		}	
	}

	protected void returnUserAnswerFeedback(UserAnswer ua, UserAnswerFeedback uaf, 
			boolean isExploit, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json;charset=UTF-8");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("userAnswer", ua);
		result.put("userAnswerFeedback", uaf);
		result.put("exploit", isExploit);
		new Gson().toJson(result, resp.getWriter());
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
