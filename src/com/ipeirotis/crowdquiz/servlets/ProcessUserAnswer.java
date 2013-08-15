package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizQuestionRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.QuizPerformance;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.utils.Helper;

@SuppressWarnings("serial")
public class ProcessUserAnswer extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("application/json");

		User user = User.getUseridFromCookie(req, resp);
		String relation = req.getParameter("relation");
		String mid = req.getParameter("mid");
		String action, useranswer=null;
		String idk = req.getParameter("idk");
		if (idk==null) {
			action = "Submit";
			int limit=4;
			for (int i=0; i<limit; i++) {
				useranswer = req.getParameter("useranswer"+i);
				if (useranswer!=null) break;
			}
		} else {
			action = "I don't know";
			useranswer = "";
		}
		
		String gold = req.getParameter("gold");
		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		if (referer==null) referer="";
		Long timestamp = (new Date()).getTime();
		Boolean isCorrect = useranswer.equals(gold);

		
		quickUpdateQuizPerformance(user, relation, isCorrect, action);
		
		storeUserAnswer(user, relation, mid, action, useranswer, ipAddress, browser, referer, timestamp, isCorrect);

		updateQuizPerformance(user, relation);

		QuizQuestion question = QuizQuestionRepository.getQuizQuestion(relation, mid);

		Integer total = question.getNumberOfUserAnswers();
		if (total == null)
			total = 0;
		Integer correct = question.getNumberOfCorrentUserAnswers();
		if (correct == null)
			correct = 0;

		String multChoiceURL = Helper.getNextMultipleChoiceURL(req, relation, user.getUserid(), mid);
		String feedbackURL = multChoiceURL
				+ "&useranswer=" + URLEncoder.encode(useranswer, "UTF-8") 
				+ "&goldprior=" + URLEncoder.encode(gold, "UTF-8") 
				+ "&iscorrect=" + URLEncoder.encode(isCorrect.toString(), "UTF-8")
				+ "&totalanswers=" + URLEncoder.encode(total.toString(), "UTF-8") 
				+ "&correctanswers=" + URLEncoder.encode(correct.toString(), "UTF-8"); 

		resp.sendRedirect(feedbackURL);

		return;

	}

	private void updateQuizPerformance(User user, String relation) {
		Queue queueUserStats = QueueFactory.getQueue("updateUserStatistics");
		queueUserStats.add(Builder.withUrl("/api/updateUserQuizStatistics")
				.param("quiz", relation)
				.param("userid", user.getUserid())
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
	private void storeUserAnswer(User user, String relation, String mid, String action, String useranswer,
			String ipAddress, String browser, String referer, Long timestamp, Boolean isCorrect) {

		Queue queueAnswers = QueueFactory.getQueue("answers");
		queueAnswers.add(Builder.withUrl("/addUserAnswer")
				.param("relation", relation)
				.param("userid", user.getUserid())
				.param("action", action)
				.param("mid", mid)
				.param("useranswer", useranswer)
				.param("correct", isCorrect.toString())
				.param("browser", browser)
				.param("ipAddress", ipAddress)
				.param("referer", referer)
				.param("timestamp", timestamp.toString())
				.method(TaskOptions.Method.POST));
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
	private void quickUpdateQuizPerformance(User user, String relation, Boolean isCorrect, String action) {

		QuizPerformance qp = QuizPerformanceRepository.getQuizPerformance(relation, user.getUserid());
		if (qp==null) {
			qp = new QuizPerformance(relation, user.getUserid());
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
