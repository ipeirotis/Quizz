package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.QuizPerformance;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class UpdateAllUserStatistics extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		queue.add(Builder.withUrl("/api/updateAllUserStatistics")
				.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
				.method(TaskOptions.Method.POST));
		
		response.getWriter().println("Process started.");
	}
	
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery(QuizPerformance.class);
		List<QuizPerformance> qplist = new ArrayList<QuizPerformance>();
		int limit = 1000;
		int i=0;
		while (true) {
			query.setRange(i, i+limit);
			@SuppressWarnings("unchecked")
			List<QuizPerformance> results = (List<QuizPerformance>) query.execute();
			if (results.size()==0) break;
			qplist.addAll(results);
			i+=limit;
		}
		
		//query = pm.newQuery(Quiz.class);
		//List<Quiz> quizlist = (List<Quiz>) query.execute();
		
		pm.close();

		for (QuizPerformance qp : qplist) {
			//for (Quiz quiz : quizlist) {
				queue.add(Builder.withUrl("/api/updateUserQuizStatistics")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("userid", qp.getUserid())
					.param("quiz", qp.getQuiz())
					.method(TaskOptions.Method.POST));
			//}
		}
	}

}
