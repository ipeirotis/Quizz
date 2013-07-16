package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.GoldAnswer;
import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class DeleteQuiz extends HttpServlet {

	final static Logger					logger						= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("text/plain");

		try {
			String relation = req.getParameter("relation");
			if (relation != null) {
				resp.getWriter().println("Deleting Quiz ID: " + relation);
			} else {
				return;
			}
			
			Quiz quiz = null;
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
			} catch (Exception e) {

			}
			pm.deletePersistent(quiz);
			
			Query q = pm.newQuery(QuizQuestion.class);
			q.setFilter("relation == relationParam");
			q.declareParameters("String relationParam");
			List<QuizQuestion> questions = (List<QuizQuestion>) q.execute(relation);
			pm.deletePersistentAll(questions);
			
			q = pm.newQuery(GoldAnswer.class);
			q.setFilter("relation == relationParam");
			q.declareParameters("String relationParam");
			List<GoldAnswer> gold = (List<GoldAnswer>) q.execute(relation);
			pm.deletePersistentAll(gold);
			
			
			q = pm.newQuery(SilverAnswer.class);
			q.setFilter("relation == relationParam");
			q.declareParameters("String relationParam");
			List<SilverAnswer> silver = (List<SilverAnswer>) q.execute(relation);
			pm.deletePersistentAll(silver);
			
			q = pm.newQuery(UserAnswer.class);
			q.setFilter("relation == relationParam");
			q.declareParameters("String relationParam");
			List<UserAnswer> useranswers = (List<UserAnswer>) q.execute(relation);
			pm.deletePersistentAll(useranswers);
			
			pm.close();
			
			/*
			String freebasetype = req.getParameter("fbtype");
			if (freebasetype != null) {
				resp.getWriter().println("Answer Freebase-Type: " + freebasetype);
			} else {
				return;
			}
			*/


			String budget = req.getParameter("budget");
			if (budget != null) {
				resp.getWriter().println("Budget: " + budget);
			} else {
				return;
			}
			
			

			
			Queue queueAdCampaign = QueueFactory.getQueue("adcampaign");
			
			// We introduce a delay of a few secs to allow the quiz to be created
			// and stored to the datastore
			long delay = 5; // in seconds
			long etaMillis = System.currentTimeMillis() + delay * 1000L;
			queueAdCampaign.add(Builder.withUrl("/addCampaign")
					.param("relation", relation)
					.param("budget", budget)
					.method(TaskOptions.Method.POST)
					.etaMillis(etaMillis));


		} catch (Exception e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
		}
	}

}
