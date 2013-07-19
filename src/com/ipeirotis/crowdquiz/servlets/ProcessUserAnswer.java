package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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

		String				url;
		String				feedback;

		Response(String url, String feedback) {
			this.url = url;
			this.feedback = feedback;
		}
	}


	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("application/json");

		User user = User.getUseridFromCookie(req, resp);
		String relation = req.getParameter("relation");
		String mid = req.getParameter("mid");
		String useranswer = req.getParameter("useranswer");
		if (useranswer == null) {
			useranswer = "";
		}
		String gold = req.getParameter("gold");
		
		String action = req.getParameter("action");
		if (action.equals("I don't know")) {
			useranswer = "";
		}
		
		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		Long timestamp = (new Date()).getTime();
		
		Boolean isCorrect = useranswer.equals(gold);

		Queue queueAnswers = QueueFactory.getQueue("answers");
		queueAnswers.add(Builder.withUrl("/addUserAnswer").
				param("relation", relation).
				param("userid", user.getUserid()).
				param("action", action).
				param("mid", mid).
				param("useranswer", useranswer).
				param("correct", isCorrect.toString() ).
				param("browser", browser).
				param("ipAddress", ipAddress).
				param("referer", referer).
				param("timestamp", timestamp.toString()).
				method(TaskOptions.Method.POST));
		
		updateQuizPerformance(user, relation, isCorrect);
		
		Queue queueUserStats = QueueFactory.getQueue("updateUserStatistics");
		queueUserStats.add(Builder.withUrl("/api/updateUserStatistics")
				.param("quiz", relation)
				.param("userid", user.getUserid())
				.method(TaskOptions.Method.GET));
		
		Gson gson = new Gson();
		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		String nextURL = baseURL + Helper.getNextURL(relation, user.getUserid(), mid);

		String message = getFeedbackMessage(user, relation, mid, useranswer, gold);
		Response result = new Response(nextURL, message);
		String json = gson.toJson(result);
		System.out.println(json);
		resp.getWriter().println(json);
		resp.sendRedirect(nextURL);
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
	
	private String getFeedbackMessage(User user, String relation, String mid, String answer, String gold) {
		
		String message = "";
		
		if (answer.equals("")) return message;
		
		
			if (gold.equals(answer)) {
				message += "Your answer '" + answer + "' is correct!\n";
			} else {
				message += "Your answer '" + answer + "' is incorrect!\n";
			}
			
		//}
		return message;
	}



}
