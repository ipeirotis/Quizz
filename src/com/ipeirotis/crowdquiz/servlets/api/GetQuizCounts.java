package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class GetQuizCounts extends HttpServlet {

		class Response {
			String				quiz;
			Integer				questions;
			Integer				gold;
			Integer				silver;
			Integer				submitted;
			
			Response(String quiz, Integer questions, Integer gold, Integer silver, Integer submitted) {
				this.quiz = quiz;
				this.questions = questions;
				this.gold = gold;
				this.silver = silver;
				this.submitted = submitted;
			}
		}

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

			String quiz = req.getParameter("quiz");
			String cache = req.getParameter("cache");
			boolean useCache = true;
			if (cache!=null && cache.equals("no")) {
				useCache = false;
			}
			
			Integer questions = QuizRepository.getNumberOfQuizQuestions(quiz, useCache);
			Integer gold = QuizRepository.getNumberOfGoldAnswers(quiz, useCache);
			Integer silver = QuizRepository.getNumberOfSilverAnswers(quiz, useCache);
			Integer submitted = QuizRepository.getNumberOfUserAnswers(quiz, useCache);
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, questions, gold, silver, submitted);
			String json = gson.toJson(result);
			resp.getWriter().println(json);
		}
		
	}
