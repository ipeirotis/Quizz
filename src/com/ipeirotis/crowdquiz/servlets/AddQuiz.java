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
			String relation = req.getParameter("relation").trim();
			resp.getWriter().println("Adding Quiz ID: " + relation);
			
			String name = req.getParameter("name").trim();
			resp.getWriter().println("Quiz Name: " + name);

			String text = req.getParameter("text").trim();
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
			
			
			EasyParamManager paramManager =
					new EasyParamManager(Builder.withUrl("/addAdGroup"), req, resp);
			paramManager
				.param("adheadline", "adText")
				.param("adline1", "adText")
				.param("adline2", "adText")
				.param("cpcbid", "CPC bid")
				.param("keywords", "AdKeywords");
			
			Queue queueAdgroup  = QueueFactory.getQueue("adgroup");
			delay = 10; // in seconds
			etaMillis = System.currentTimeMillis() + delay * 1000L;
			queueAdgroup.add(paramManager.getTaskOptions()
					.param("relation", relation)
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
	
	protected static class EasyParamManager {
		protected TaskOptions taskOptions;
		protected HttpServletRequest req;
		protected HttpServletResponse resp;
		
		public EasyParamManager(TaskOptions taskOptions, HttpServletRequest req, HttpServletResponse resp){
			this.taskOptions = taskOptions;
			this.req = req;
			this.resp = resp;
		}
		
		public EasyParamManager param(String paramName, String outputName) throws IOException{
			return param(paramName, paramName, outputName);
		}
		
		public EasyParamManager param(String reqParamName, String paramName, String outputName) throws IOException{
			String paramValue = req.getParameter(reqParamName);
			if (paramValue != null) {
				resp.getWriter().println(outputName + ": " + paramValue);
				taskOptions.param(paramName, paramValue);
			} else {
				resp.getWriter().println("Missed: " + reqParamName + "/" + outputName + "  IGNORING");
			}
			return this;
		}
		
		public TaskOptions getTaskOptions(){
			return taskOptions;
		}
	}
}
