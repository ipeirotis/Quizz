package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class GetNumberOfSilverAnswers extends HttpServlet {

		class Response {
			String				quiz;
			String				questions;
			
			Response(String quiz, String questions) {
				this.quiz = quiz;
				this.questions = questions;
			}
		}

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

			String nocache = req.getParameter("nocache");
			boolean useCache = true;
			if (nocache!=null && nocache.equals("yes")) {
				useCache = false;
			}
			
			String quiz = req.getParameter("quiz");
			String key = "silveranswers_"+quiz;
			
			String questions = CachePMF.get(key, String.class);
			if (questions == null || !useCache) {
				questions = getNumberOfQuizQuestions(quiz);
				CachePMF.put(key, questions);
			}
			
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, questions);
			String json = gson.toJson(result);
			resp.getWriter().println(json);

		}
		
		private String getNumberOfQuizQuestions(String quiz) {
			PersistenceManager	pm = PMF.get().getPersistenceManager();
			Query q = pm.newQuery(SilverAnswer.class);
			q.setFilter("relation == lastNameParam");
			q.declareParameters("String lastNameParam");
			List<SilverAnswer> results = (List<SilverAnswer>) q.execute(quiz);
			Integer numQuestions = results.size();
			return numQuestions.toString();
		}
	}
