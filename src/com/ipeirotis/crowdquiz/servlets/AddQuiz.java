package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.utils.Helper;

@SuppressWarnings("serial")
public class AddQuiz extends HttpServlet {

	final static Logger					logger						= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Utils.ensureParameters(req, "relation", "name", "text");
		try {
			String relation = req.getParameter("relation");
			resp.getWriter().println("Adding Quiz ID: " + relation);
			
			String name = req.getParameter("name");
			resp.getWriter().println("Quiz Name: " + name);

			String text = req.getParameter("text");
			resp.getWriter().println("Question Text: " + text);

			Quiz q = new Quiz(name, relation, text);
			QuizRepository.storeQuiz(q);

			String budget = req.getParameter("budget");
			if (budget != null) {
				resp.getWriter().println("Budget: " + budget);
			} else {
				// return;
			}
			
			Queue queueAdCampaign = QueueFactory.getQueue("adcampaign");
			
			// We introduce a delay of a few secs to allow the quiz to be created
			// and stored to the datastore
			long delay = 0; // in seconds
			long etaMillis = System.currentTimeMillis() + delay * 1000L;
			queueAdCampaign.add(Builder.withUrl("/addCampaign")
					.param("relation", relation)
					.param("budget", budget)
					.method(TaskOptions.Method.POST)
					.etaMillis(etaMillis));
			
			
			
			String adheadline = req.getParameter("adheadline");
			if (adheadline != null) {
				resp.getWriter().println("adText: " + adheadline);
			} else {
				// return;
			}
			
			String adline1 = req.getParameter("adline1");
			if (adline1 != null) {
				resp.getWriter().println("adText: " + adline1);
			} else {
				// return;
			}
			
			String adline2 = req.getParameter("adline2");
			if (adline2 != null) {
				resp.getWriter().println("adText: " + adline2);
			} else {
				// return;
			}
			
			
			String cpcbid = req.getParameter("cpcbid");
			if (cpcbid != null) {
				resp.getWriter().println("CPC bid: " + cpcbid);
			} else {
				// return;
			}
			
			String keywords = req.getParameter("keywords");
			if (keywords != null) {
				resp.getWriter().println("AdKeywords: " + keywords);
			} else {
				// return;
			}

			Queue queueAdgroup  = QueueFactory.getQueue("adgroup");
			delay = 10; // in seconds
			etaMillis = System.currentTimeMillis() + delay * 1000L;
			queueAdgroup.add(Builder.withUrl("/addAdGroup")
					.param("relation", relation)
					.param("cpcbid", cpcbid)
					.param("keywords", keywords)
						.param("adheadline", adheadline)
						.param("adline1", adline1)
						.param("adline2", adline2)
					.method(TaskOptions.Method.POST)
					.etaMillis(etaMillis));

			resp.setContentType("text/plain");
			String baseURL = Helper.getBaseURL(req);
			String url = baseURL + "/admin/manage/";
			resp.sendRedirect(url); 
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
		}
	}

}
