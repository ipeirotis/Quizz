package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.Quiz;


@SuppressWarnings("serial")
public class GetQuizCounts extends HttpServlet {

		class Response {
			String				quiz;
			Integer				questions;
			Integer				gold;
			Integer				submitted;
			
			Response(String quiz, Integer questions, Integer gold, Integer submitted) {
				this.quiz = quiz;
				this.questions = questions;
				this.gold = gold;
				
				this.submitted = submitted;
			}
		}

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

			String quiz = req.getParameter("quizID");
			String cache = req.getParameter("cache");
			if (cache!=null && cache.equals("no")) {
				QuizRepository.updateQuizCounts(quiz);
			}

			Quiz q = QuizRepository.getQuiz(quiz);
			Preconditions.checkArgument(q != null, "Unknown quiz ID: " + quiz);
			
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, q.getQuestions(),
					q.getGold(),  q.getSubmitted());
			String json = gson.toJson(result);
			resp.getWriter().println(json);
		}
}
