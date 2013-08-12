package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.servlets.api.GetNumberOfGoldAnswers.Response;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class GetQuizCounts extends HttpServlet {

		class Response {
			String				quiz;
			String				questions;
			String				gold;
			String				silver;
			String				submitted;
			
			Response(String quiz, String questions, String gold, String silver, String submitted) {
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
			
			String questions = QuizRepository.getNumberOfQuizQuestions(quiz, useCache).toString();
			String gold = QuizRepository.getNumberOfGoldAnswers(quiz, useCache).toString();
			String silver = QuizRepository.getNumberOfSilverAnswers(quiz, useCache).toString();
			String submitted = QuizRepository.getNumberOfUserAnswers(quiz, useCache).toString();
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, questions, gold, silver, submitted);
			String json = gson.toJson(result);
			resp.getWriter().println(json);
		}
		
	}
