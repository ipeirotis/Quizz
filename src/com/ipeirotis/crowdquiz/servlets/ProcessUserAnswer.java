package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.QuizPerformance;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.utils.Helper;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class ProcessUserAnswer extends HttpServlet {

	class Response {

		Boolean	showFeedbackURL;
		String	nextMultChoiceURL;
		String	feedbackURL;

		Response(Boolean showFeedbackURL, String nextMultChoiceURL, String feedbackURL) {

			this.showFeedbackURL = showFeedbackURL;
			this.nextMultChoiceURL = nextMultChoiceURL;
			this.feedbackURL = feedbackURL;
		}
	}

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
			int limit=Integer.parseInt(req.getParameter("numoptions"));
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

		updateQuizPerformance(user, relation, isCorrect);

		Queue queueUserStats = QueueFactory.getQueue("updateUserStatistics");
		queueUserStats.add(Builder.withUrl("/api/updateUserQuizStatistics")
				.param("quiz", relation)
				.param("userid", user.getUserid())
				.method(TaskOptions.Method.POST));

		QuizQuestion question = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			question = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(relation, mid));
		} finally {
			pm.close();
		}
		Integer total = question.getNumberOfUserAnswers();
		if (total == null)
			total = 0;
		Integer correct = question.getNumberOfCorrentUserAnswers();
		if (correct == null)
			correct = 0;

		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		String multChoiceURL = baseURL + Helper.getNextMultipleChoiceURL(relation, user.getUserid(), mid);
		String feedbackURL = multChoiceURL
				+ "&useranswer=" + URLEncoder.encode(useranswer, "UTF-8") 
				+ "&goldprior=" + URLEncoder.encode(gold, "UTF-8") 
				+ "&iscorrect=" + URLEncoder.encode(isCorrect.toString(), "UTF-8")
				+ "&totalanswers=" + URLEncoder.encode(total.toString(), "UTF-8") 
				+ "&correctanswers=" + URLEncoder.encode(correct.toString(), "UTF-8"); 


		Boolean showFeedback = user.getsTreatment("showMessage");

		Response result = new Response(showFeedback, multChoiceURL, feedbackURL);
		Gson gson = new Gson();
		String json = gson.toJson(result);
		resp.getWriter().println(json);

		resp.sendRedirect(feedbackURL);

		return;

	}

	private void updateQuizPerformance(User user, String relation, Boolean isCorrect) {

		QuizPerformance qp = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			qp = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(relation, user.getUserid()));
		} catch (Exception e) {
			qp = new QuizPerformance(relation, user.getUserid());
		}
		if (isCorrect) {
			qp.increaseCorrect();
		}
		qp.increaseTotal();
		pm.makePersistentAll(qp);
		pm.close();
	}

}
