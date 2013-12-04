package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;
import us.quizz.repository.QuizesOperations;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.Quiz;

@SuppressWarnings("serial")
public class UpdateCountStatistics extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		Queue queue = QueueFactory.getQueue("default");
		List<Quiz> list = QuizRepository.getQuizzes();

		for (Quiz quiz : list) {
			
			resp.getWriter().println("Updating quiz: " + quiz.getName());
			
			queue.add(Builder.withUrl("/api/getQuizCounts")
					.param("quizID", quiz.getQuizID())
					.param("cache", "no")
					.method(TaskOptions.Method.GET));
			
			// TODO: This is piggybagging an existing cron effort
			// to cache the creation of quizzes with 10 questions.
			// We should move that into an independent cron call
			//QuizesOperations.getNextQuizQuestions(quiz.getQuizID(), 10);
			
		}
	}
}
