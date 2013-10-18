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
import com.ipeirotis.crowdquiz.entities.Question;

@SuppressWarnings("serial")
public class UpdateAllQuestionStatistics extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		queue.add(Builder.withUrl("/api/updateAllQuestionStatistics")
				.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
				.method(TaskOptions.Method.POST));
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		
		List<Question> quizquestions = QuizQuestionRepository.getQuizQuestions();
		
		for (Question quizquestion : quizquestions) {
			queue.add(Builder.withUrl("/api/updateQuestionStatistics")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("questionID", quizquestion.getKey().toString())
					.method(TaskOptions.Method.POST));
		}
	}


}
