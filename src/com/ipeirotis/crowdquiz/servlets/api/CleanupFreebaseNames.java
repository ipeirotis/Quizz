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
import com.google.common.base.Strings;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.servlets.Utils;
import com.ipeirotis.crowdquiz.utils.FreebaseSearch;

@SuppressWarnings("serial")
public class CleanupFreebaseNames extends HttpServlet{

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Queue queue = QueueFactory.getQueue("freebaseNamesUpdate");
		QuizQuestion qq = new QuizQuestion("song", "/m/07rd7", null, 0.5);
		QuizQuestionRepository.storeQuizQuestion(qq);
		queue.add(Builder.withUrl("/api/cleanupFreebaseNames")
				.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
				.method(TaskOptions.Method.POST));
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Queue queue = QueueFactory.getQueue("freebaseNamesUpdate");
		
		List<QuizQuestion> quizquestions = QuizQuestionRepository.getQuizQuestions();
		
		for (QuizQuestion quizQuestion : quizquestions) {
			if (Strings.isNullOrEmpty(quizQuestion.getName())) {
				queue.add(Builder.withUrl("/api/cleanupFreebaseNames")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("relation", quizQuestion.getRelation())
					.param("mid", quizQuestion.getFreebaseEntityId())
					.method(TaskOptions.Method.PUT));
			}
		}
	}
	
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Utils.ensureParameters(req, "mid", "relation");
		String mid = req.getParameter("mid");
		String quizId = req.getParameter("relation");
		QuizQuestion quizQuestion = QuizQuestionRepository.getQuizQuestion(quizId, mid);
		System.out.println("WORKING ON: " + mid + " : " + quizId);
		String name = FreebaseSearch.getFreebaseAttribute(mid,"name");
		System.out.println("GOT NAME: " + name);
		if (Strings.isNullOrEmpty(name)) {
			// TODO delete
		} else {
			quizQuestion.setName(name);
			QuizQuestionRepository.storeQuizQuestion(quizQuestion);
		}
	}
}
