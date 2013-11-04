package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.AnswersRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.Answer;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.entities.QuizPerformance;
import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswerFeedback;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class ProcessUserAnswer extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Utils.ensureParameters(req,
				"quizID", "questionID", "answerID", "correctanswers", "totalanswers");

		User user = UserRepository.getUseridFromCookie(req, resp);
		Long questionID = Long.parseLong(req.getParameter("questionID"));
		String quizID = req.getParameter("quizID");
		String action;
		Integer useranswerID = Integer.parseInt(req.getParameter("answerID"));
		Boolean isCorrect = false;
		if (useranswerID != -1) {
			action = "Submit";
			Answer answer = AnswersRepository.getAnswer(questionID, useranswerID);
			if (answer.getIsGold() != null) {
				isCorrect = answer.getIsGold();
			}
		} else {
			action = "I don't know";
		}
		String numCorrectAnswers = req.getParameter("correctanswers");
		String numTotalAnswers = req.getParameter("totalanswers");
		
		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		if (referer==null) referer="";
		Long timestamp = (new Date()).getTime();

		UserAnswerFeedback uaf = createUserAnswerFeedback(user, questionID, useranswerID,
				isCorrect, numCorrectAnswers, numTotalAnswers);
		quickUpdateQuizPerformance(user, quizID, isCorrect, action);
		storeUserAnswer(user, quizID, questionID, action, useranswerID, ipAddress, browser,
				referer, timestamp, isCorrect);
		updateQuizPerformance(user, questionID);
		returnUserAnswerFeedback(uaf, resp);
	}

	protected void returnUserAnswerFeedback(UserAnswerFeedback uaf,
			HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		new Gson().toJson(uaf, resp.getWriter());
	}

	protected UserAnswerFeedback createUserAnswerFeedback(User user, Long questionID, Integer useranswerID,
			Boolean isCorrect, String numCorrectAnswers, String numTotalAnswers) {
		UserAnswerFeedback uaf = new UserAnswerFeedback(questionID, user.getUserid(), useranswerID, isCorrect);
		if (!Strings.isNullOrEmpty(numCorrectAnswers)) uaf.setNumCorrectAnswers(Integer.parseInt(numCorrectAnswers));
		if (!Strings.isNullOrEmpty(numTotalAnswers)) uaf.setNumTotalAnswers(Integer.parseInt(numTotalAnswers));
		Question question = QuizQuestionRepository.getQuizQuestion(questionID);
		uaf.setUserAnswerText((useranswerID == -1)? "" : question.getAnswer(useranswerID).getText());
		uaf.setCorrectAnswerText(question.goldAnswer().getText());
		uaf.computeDifficulty();
		UserAnswerRepository.storeUserAnswerFeedback(uaf);
		return uaf;
	}

	private void updateQuizPerformance(User user, Long questionID) {
		Queue queueUserStats = QueueFactory.getQueue("updateUserStatistics");
		String quizID = QuizQuestionRepository.getQuizQuestion(questionID).getQuizID();
		queueUserStats.add(Builder.withUrl("/api/updateUserQuizStatistics")
				.param("quiz", quizID)
				.param("userid", user.getUserid())
				.param("channelNotify", "true")
				.method(TaskOptions.Method.POST));
	}

	/**
	 * @param user
	 * @param relation
	 * @param mid
	 * @param action
	 * @param useranswer
	 * @param ipAddress
	 * @param browser
	 * @param referer
	 * @param timestamp
	 * @param isCorrect
	 */
	private void storeUserAnswer(User user, String quizID, Long questionID, String action, Integer useranswerID,
			String ipAddress, String browser, String referer, Long timestamp, Boolean isCorrect) {

		UserAnswer ue = new UserAnswer(user.getUserid(), questionID, useranswerID);
		ue.setReferer(referer);
		ue.setBrowser(browser);
		ue.setIpaddress(ipAddress);
		ue.setTimestamp(timestamp);
		ue.setAction(action);
		ue.setIsCorrect(isCorrect);
		ue.setQuizID(quizID);
		PMF.singleMakePersistent(ue);
	}

	/**
	 * With this call, we just update the counts of correct and incorrect answers.
	 * The full update happens asynchronously from the updateUserStatistics call that is
	 * placed in the task queue.
	 * 
	 * @param user
	 * @param relation
	 * @param isCorrect
	 * @param action
	 */
	private void quickUpdateQuizPerformance(User user, String quizID, Boolean isCorrect, String action) {

		QuizPerformance qp = QuizPerformanceRepository.getQuizPerformance(quizID, user.getUserid());
		if (qp==null) {
			qp = new QuizPerformance(quizID, user.getUserid());
		}

		if (isCorrect) {
			qp.increaseCorrect();
		}
		if (action.equals("Submit")) {
			qp.increaseTotal();
		}
		QuizPerformanceRepository.cacheQuizPerformance(qp);
		
	}

}
