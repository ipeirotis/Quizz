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

import us.quizz.repository.UserAnswerRepository;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class GetNumberOfSubmittedAnswers extends HttpServlet {

		class Response {
			String				quiz;
			Integer				answers;
			
			Response(String quiz, Integer answers) {
				this.quiz = quiz;
				this.answers = answers;
			}
		}

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

     
      String quiz = req.getParameter("quiz");
			String userid = req.getParameter("userid");
			
			Integer answers = UserAnswerRepository.getUserAnswers(quiz, userid).size();
			
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, answers);
			String json = gson.toJson(result);
			resp.getWriter().println(json);

		}
		

	}
