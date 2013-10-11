package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizPerformanceRepository;

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.QuizPerformance;

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
		List<QuizPerformance> qplist = QuizPerformanceRepository.getQuizPerformances();

		for (QuizPerformance qp : qplist) {
			queue.add(Builder.withUrl("/api/updateUserQuizStatistics")
				.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
				.param("userid", qp.getUserid())
				.param("quiz", qp.getQuiz())
				.method(TaskOptions.Method.POST));
		}
	}

}
