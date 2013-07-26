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

import com.ipeirotis.crowdquiz.entities.GoldAnswer;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class UpdateQuestionStatistics extends HttpServlet {



	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		String relation = req.getParameter("relation");
		String mid = req.getParameter("mid");
		
		resp.getWriter().print("Relation:"+relation+"\n");
		resp.getWriter().print("Mid:"+mid+"\n");

		QuizQuestion question = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			question = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(relation, mid));
		} catch (Exception e) {
			resp.getWriter().print("NOT FOUND!\n");
			return;
		}
		resp.getWriter().print("FOUND!\n");
		
		int g = getNumberOfGoldAnswers(relation, mid);
		question.setHasGoldAnswer((g>0));
		question.setNumberOfGoldAnswers(g);
		resp.getWriter().print("Number of gold answers:"+g+"\n");
		
		int s = getNumberOfSilverAnswers(relation, mid);
		question.setHasSilverAnswers((s>0));
		question.setNumberOfSilverAnswers(s);
		resp.getWriter().print("Number of silver answers:"+s+"\n");
		
		int u = getNumberOfUserAnswers(relation, mid);
		question.setHasUserAnswers((u>0));
		question.setNumberOfUserAnswers(u);
		resp.getWriter().print("Number of user answers:"+u+"\n");

		int c = getNumberOfCorrectUserAnswers(relation, mid);
		question.setNumberOfCorrentUserAnswers(c);
		resp.getWriter().print("Number of correct user answers:"+c+"\n");
		
		pm.makePersistent(question);
		pm.close();
		
		

	}
	
	private int getNumberOfGoldAnswers(String quiz, String mid) {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String,Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);
		params.put("midParam", mid);
  
		List<GoldAnswer> results = (List<GoldAnswer>) q.executeWithMap(params);
		return results.size();
	}
	
	private int getNumberOfSilverAnswers(String quiz, String mid) {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(SilverAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String,Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);
		params.put("midParam", mid);
  
		List<SilverAnswer> results = (List<SilverAnswer>) q.executeWithMap(params);
		return results.size();
	}
	
	private int getNumberOfUserAnswers(String quiz, String mid) {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam && action==submitParam");
		q.declareParameters("String quizParam, String midParam, String submitParam");

		Map<String,Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);
		params.put("midParam", mid);
		params.put("submitParam", "Submit");
  
		List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
		return results.size();
	}
	
	private int getNumberOfCorrectUserAnswers(String quiz, String mid) {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam && action==submitParam && isCorrect==correctParam");
		q.declareParameters("String quizParam, String midParam, String submitParam, Boolean correctParam");

		Map<String,Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);
		params.put("midParam", mid);
		params.put("submitParam", "Submit");
		params.put("correctParam", Boolean.TRUE);
  
		List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
		return results.size();
	}


	
	
}
