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

		String baseURL = req.getScheme() + "://" + req.getServerName(); 
		String url = baseURL + "/admin/";
		resp.sendRedirect(url); 
		

		try {
			String relation = req.getParameter("relation");
			if (relation == null) {
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
			@SuppressWarnings("unchecked")
			List<QuizQuestion> questions = (List<QuizQuestion>) q.execute(relation);
			pm.deletePersistentAll(questions);
			
			q = pm.newQuery(GoldAnswer.class);
			q.setFilter("relation == relationParam");
			q.declareParameters("String relationParam");
			@SuppressWarnings("unchecked")
			List<GoldAnswer> gold = (List<GoldAnswer>) q.execute(relation);
			pm.deletePersistentAll(gold);
			
			
			q = pm.newQuery(SilverAnswer.class);
			q.setFilter("relation == relationParam");
			q.declareParameters("String relationParam");
			@SuppressWarnings("unchecked")
			List<SilverAnswer> silver = (List<SilverAnswer>) q.execute(relation);
			pm.deletePersistentAll(silver);
			
			q = pm.newQuery(UserAnswer.class);
			q.setFilter("relation == relationParam");
			q.declareParameters("String relationParam");
			@SuppressWarnings("unchecked")
			List<UserAnswer> useranswers = (List<UserAnswer>) q.execute(relation);
			pm.deletePersistentAll(useranswers);
			
			pm.close();
			

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
		}
	}

}
