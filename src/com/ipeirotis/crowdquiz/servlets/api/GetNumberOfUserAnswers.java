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

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class GetNumberOfUserAnswers extends HttpServlet {

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

      Cache cache;
      Map props = new HashMap();
      props.put(GCacheFactory.EXPIRATION_DELTA, 300);

      try {
          CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
          cache = cacheFactory.createCache(props);
      } catch (CacheException e) {
          cache = null;
      }
      
			String quiz = req.getParameter("quiz");
			String key = "usersanswers_"+quiz;
			String nocache = req.getParameter("nocache");
			boolean useCache = true;
			if (nocache!=null && nocache.equals("yes")) {
				useCache = false;
			}
			
			String questions;
			if (cache!=null && useCache && cache.containsKey(key)) {
				byte[] value = (byte[])cache.get(key);
				questions = new String(value);
			} else {
				questions = getNumberOfQuizQuestions(quiz);
				cache.put(key, questions.getBytes());
			}
			
			resp.setContentType("application/json");
			Gson gson = new Gson();
			Response result = new Response(quiz, questions);
			String json = gson.toJson(result);
			resp.getWriter().println(json);

		}
		
		private String getNumberOfQuizQuestions(String quiz) {
			PersistenceManager	pm = PMF.get().getPersistenceManager();
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("relation == lastNameParam");
			q.declareParameters("String lastNameParam");
			List<UserAnswer> results = (List<UserAnswer>) q.execute(quiz);
			Integer numQuestions = results.size();
			return numQuestions.toString();
		}
	}
