package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

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
		int counts = 0;
		PrintWriter pw = resp.getWriter();
		pw.println("Quiz questions with empty names:");
		for (QuizQuestion quizQuestion : QuizQuestionRepository.getQuizQuestions()) {
			if (Strings.isNullOrEmpty(quizQuestion.getName())) {
				queue.add(Builder.withUrl("/api/cleanupFreebaseNames")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("relation", quizQuestion.getRelation())
					.param("mid", quizQuestion.getFreebaseEntityId())
					.method(TaskOptions.Method.PUT));
				pw.println(quizQuestion.getRelation() + " : " + quizQuestion.getFreebaseEntityId() + " : ");
				counts++;
			}
		}
		resp.getWriter().println("Total: " + counts);
	}
	
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Utils.ensureParameters(req, "mid", "relation");
		String mid = req.getParameter("mid");
		String quizId = req.getParameter("relation");
		String name = FreebaseSearch.getFreebaseAttribute(mid,"name");
		if (Strings.isNullOrEmpty(name)) {
			QuizQuestionRepository.removeWithoutUpdates(quizId, mid);
			QuizRepository.updateQuizCounts(quizId);
		} else {
			QuizQuestion quizQuestion = QuizQuestionRepository.getQuizQuestion(quizId, mid);
			quizQuestion.setName(name);
			QuizQuestionRepository.storeQuizQuestion(quizQuestion);
		}
	}
}
