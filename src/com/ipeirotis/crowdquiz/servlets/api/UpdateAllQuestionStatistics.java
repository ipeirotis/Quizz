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
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class UpdateAllQuestionStatistics extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		Queue queue = QueueFactory.getQueue("default");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(QuizQuestion.class);
		List<QuizQuestion> list = (List<QuizQuestion>) q.execute();
		pm.close();

		for (QuizQuestion quizquestion : list) {
			queue.add(Builder.withUrl("/api/updateQuestionStatistics")
					.param("relation", quizquestion.getRelation())
					.param("mid", quizquestion.getFreebaseEntityId())
					.method(TaskOptions.Method.GET));
		}
	}

}
