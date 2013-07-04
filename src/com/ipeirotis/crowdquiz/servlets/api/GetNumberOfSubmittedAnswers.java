package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class GetNumberOfSubmittedAnswers extends HttpServlet {

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
			String userid = req.getParameter("userid");
			Integer questions = getNumberOfQuizQuestions(quiz, userid);
			
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, questions);
			String json = gson.toJson(result);
			resp.getWriter().println(json);

		}
		
		private int getNumberOfQuizQuestions(String quiz, String userid) {
			PersistenceManager	pm = PMF.get().getPersistenceManager();
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("relation == quizParam && userid == useridParam");
			q.declareParameters("String quizParam, String useridParam");

			Map<String,Object> params = new HashMap<String, Object>();
			params.put("quizParam", quiz);
      params.put("useridParam", userid);
      
			List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
			return results.size();
		}
	}
