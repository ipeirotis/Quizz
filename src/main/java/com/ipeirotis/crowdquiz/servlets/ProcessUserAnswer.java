package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.UserAnswerRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.common.base.Strings;
import com.google.gson.Gson;
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
				"quizID", "questionID", "gold", "correctanswers", "totalanswers");

		User user = User.getUseridFromCookie(req, resp);
		String questionID = req.getParameter("questionID");
		String quizID = req.getParameter("quizID");
		String action, useranswer=null;
		String idk = req.getParameter("idk");
		if (idk==null) {
			action = "Submit";
			int limit=4;
			for (int i=0; i<limit; i++) {
				useranswer = req.getParameter("useranswer"+i);
				if (useranswer != null) {
					useranswer = useranswer.trim();
					break;
				}
			}
		} else {
			action = "I don't know";
			useranswer = "";
		}
		String gold = req.getParameter("gold");
		String numCorrectAnswers = req.getParameter("correctanswers");
		String numTotalAnswers = req.getParameter("totalanswers");
		
		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		if (referer==null) referer="";
		Long timestamp = (new Date()).getTime();
		Boolean isCorrect = useranswer.equals(gold);

		UserAnswerFeedback uaf = createUserAnswerFeedback(user, questionID, useranswer,
				gold, numCorrectAnswers, numTotalAnswers);
		quickUpdateQuizPerformance(user, quizID, isCorrect, action);
		storeUserAnswer(user, questionID, action, useranswer, ipAddress, browser,
				referer, timestamp, isCorrect);
		updateQuizPerformance(user, quizID);
		returnUserAnswerFeedback(uaf, resp);
	}

	protected void returnUserAnswerFeedback(UserAnswerFeedback uaf,
			HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		new Gson().toJson(uaf, resp.getWriter());
	}

	protected UserAnswerFeedback createUserAnswerFeedback(User user, String questionID, String useranswer,
			String gold, String numCorrectAnswers, String numTotalAnswers) {
		UserAnswerFeedback uaf = new UserAnswerFeedback(questionID, user.getUserid(), useranswer, gold);
		if (!Strings.isNullOrEmpty(numCorrectAnswers)) uaf.setNumCorrectAnswers(Integer.parseInt(numCorrectAnswers));
		if (!Strings.isNullOrEmpty(numTotalAnswers)) uaf.setNumTotalAnswers(Integer.parseInt(numTotalAnswers));
		uaf.computeDifficulty();
		UserAnswerRepository.storeUserAnswerFeedback(uaf);
		return uaf;
	}

	private void updateQuizPerformance(User user, String questionID) {
		Queue queueUserStats = QueueFactory.getQueue("updateUserStatistics");
		String quizID = QuizQuestionRepository.getQuizQuestion(questionID).getQuizzID();
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
	private void storeUserAnswer(User user, String questionID, String action, String useranswerID,
			String ipAddress, String browser, String referer, Long timestamp, Boolean isCorrect) {

		UserAnswer ue = new UserAnswer(user.getUserid(), questionID, useranswerID);
		ue.setReferer(referer);
		ue.setBrowser(browser);
		ue.setIpaddress(ipAddress);
		ue.setTimestamp(timestamp);
		ue.setAction(action);
		ue.setIsCorrect(isCorrect);
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
