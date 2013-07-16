package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
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

		String userid = Helper.getUseridFromCookie(req, resp);
		String relation = req.getParameter("relation");
		String mid = req.getParameter("mid");
		String useranswer = req.getParameter("useranswer");
		String action = req.getParameter("action");
		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		Long timestamp = (new Date()).getTime();

		Queue queue = QueueFactory.getQueue("answers");
		queue.add(Builder.withUrl("/addUserAnswer").
				param("relation", relation).
				param("userid", userid).
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
		String nextURL = baseURL + Helper.getNextURL(relation, userid, mid);

		String message = getFeedbackMessage("", relation, userid, mid);
		Response result = new Response(nextURL, message);
		String json = gson.toJson(result);
		System.out.println(json);
		resp.getWriter().println(json);

	}
	
	private String getFeedbackMessage(String treatment, String relation, String userid, String mid) {
		// List<String> answers = getCrowdAnswers(relation, userid, mid);
		// Set<String> goldaanswers = getGoldAnswers(relation, mid);
		
		return "Thank you for your entry!";
	}



}
