package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class GetNumberOfQuizQuestions extends HttpServlet {

		class Response {
			String				quiz;
			Integer				questions;
			
			Response(String quiz, Integer questions) {
				this.quiz = quiz;
				this.questions = questions;
			}
		}

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

			String quiz = req.getParameter("quiz");
			Integer questions = getNumberOfQuizQuestions(quiz);
			
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, questions);
			String json = gson.toJson(result);
			resp.getWriter().println(json);

		}
		
		private int getNumberOfQuizQuestions(String quiz) {
			PersistenceManager	pm = PMF.get().getPersistenceManager();
			Query q = pm.newQuery(QuizQuestion.class);
			q.setFilter("relation == lastNameParam");
			q.declareParameters("String lastNameParam");
			List<QuizQuestion> results = (List<QuizQuestion>) q.execute(quiz);
			return results.size();
		}
	}
