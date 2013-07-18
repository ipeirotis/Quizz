package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Persistent;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.Treatment;
import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
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
		
		String action = req.getParameter("action");
		if (action.equals("I don't know")) {
			useranswer = "";
		}
		
		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		Long timestamp = (new Date()).getTime();

		Queue queue = QueueFactory.getQueue("answers");
		queue.add(Builder.withUrl("/addUserAnswer").
				param("relation", relation).
				param("userid", user.getUserid()).
				param("action", action).
				param("mid", mid).
				param("useranswer", useranswer).
				param("browser", browser).
				param("ipAddress", ipAddress).
				param("referer", referer).
				param("timestamp", timestamp.toString()).
				method(TaskOptions.Method.POST));

		
		Gson gson = new Gson();
		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		String nextURL = baseURL + Helper.getNextURL(relation, user.getUserid(), mid);

		String message = getFeedbackMessage(user, relation, mid, useranswer);
		Response result = new Response(nextURL, message);
		String json = gson.toJson(result);
		System.out.println(json);
		resp.getWriter().println(json);

	}
	
	private String getFeedbackMessage(User user, String relation, String mid, String answer) {
		
		String message = "";
		
		Treatment t = user.getTreatment();
		
		// Should we show any popup with a message?
		// Boolean showMessage;
		//
		// Should we show the correct answer in the popup?
		// Boolean showCorrect;

		// Should we show the total number of correct answers so far?
		// Boolean showScore;

		// Should we show the percentage of correct answers?
		// Boolean showPercentageCorrect;

		// Should we show the answers given by other users?
		// Boolean showCrowdAnswers;

		// Should we show the rank among the other users in terms of % of correct answers?
		// Boolean showPercentageRank;

		// Should we show the rank among the other users in terms of # of correct answers?
		// Boolean showScoreRank;
		
		if (!t.getShowMessage()) {
			return null;
		}
		
		//if (t.getShowCorrect()) {
			List<String> gold = QuizQuestion.getGoldAnswers(relation, mid);
			if (gold.contains(answer)) {
				message += "Your answer '" + answer + "' is correct!\n";
			} else {
				message += "Your answer '" + answer + "' is incorrect!\n";
			}
			
		//}
		
		
		
		return message;
	}



}
