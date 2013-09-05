package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;

@SuppressWarnings("serial")
public class CleanupFreebaseNames extends HttpServlet{

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Queue queue = QueueFactory.getQueue("freebaseNamesUpdate");
		queue.add(Builder.withUrl("/api/cleanupFreebaseNames")
				.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
				.method(TaskOptions.Method.POST));
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		
		List<QuizQuestion> quizquestions = QuizQuestionRepository.getQuizQuestions();
		
		for (QuizQuestion quizquestion : quizquestions) {
			queue.add(Builder.withUrl("/api/cleanupFreebaseNames")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("relation", quizquestion.getRelation())
					.param("mid", quizquestion.getFreebaseEntityId())
					.method(TaskOptions.Method.PUT));
		}
	}
	
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
	}
}
