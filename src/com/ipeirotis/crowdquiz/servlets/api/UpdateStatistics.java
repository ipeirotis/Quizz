package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class UpdateStatistics extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		Queue queue = QueueFactory.getQueue("default");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Quiz.class);
		List<Quiz> list = (List<Quiz>) q.execute();
		pm.close();

		for (Quiz quiz : list) {
			
			resp.getWriter().println("Updating quiz: " + quiz.getName());
			
			queue.add(Builder.withUrl("/api/getNumberOfQuizQuestions")
					.param("quiz", quiz.getRelation())
					.param("nocache", "yes")
					.method(TaskOptions.Method.GET));
			
			queue.add(Builder.withUrl("/api/getNumberOfGoldAnswers")
					.param("quiz", quiz.getRelation())
					.param("nocache", "yes")
					.method(TaskOptions.Method.GET));
			
			queue.add(Builder.withUrl("/api/getNumberOfSilverAnswers")
					.param("quiz", quiz.getRelation())
					.param("nocache", "yes")
					.method(TaskOptions.Method.GET));
			
			queue.add(Builder.withUrl("/api/getNumberOfUserAnswers")
					.param("quiz", quiz.getRelation())
					.param("nocache", "yes")
					.method(TaskOptions.Method.GET));
		}
		
		

	}

}
