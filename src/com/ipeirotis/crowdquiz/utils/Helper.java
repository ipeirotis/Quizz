package com.ipeirotis.crowdquiz.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.UserAnswer;


public class Helper {



	
	/**
	 * Returns the next question for the user. Checks all the previously given answers by the user
	 * to avoid returning a question for which we already have an answer from the user. The parameter
	 * justAddedMid ensures that we do not return the currently asked question, even if the relation
	 * has not persisted in the datastore yet.
	 * 
	 * 
	 * @param relation
	 * @param userid
	 * @param justAddedMid
	 * @param pm
	 * @return
	 */
	public static String getNextMultipleChoiceURL(String relation, String userid, String justAddedMid) {
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		
		String query = "SELECT FROM " + QuizQuestion.class.getName() 
							+ " WHERE relation=='" + relation + "'"
							+ " && hasGoldAnswer==true";

		@SuppressWarnings("unchecked")
		List<QuizQuestion> questions = (List<QuizQuestion>) pm.newQuery(query).execute();
		Set<String> availableQuestions = new HashSet<String>();
		for (QuizQuestion q : questions) {
			availableQuestions.add(q.getFreebaseEntityId());
		}

		String queryGivenAnswers = "SELECT FROM " + UserAnswer.class.getName() + " WHERE userid=='" + userid
				+ "' && relation=='" + relation + "'";

		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) pm.newQuery(queryGivenAnswers).execute();
		Set<String> alreadyAnswered = new HashSet<String>();
		for (UserAnswer ue : answers) {
			alreadyAnswered.add(ue.getMid());
		}
		if (justAddedMid!=null) {
			alreadyAnswered.add(justAddedMid);
		}
		
		pm.close();
		
		availableQuestions.removeAll(alreadyAnswered);
		
		String nextURL = "/";
		if (availableQuestions.isEmpty()) {
			return nextURL;
		} else {
			ArrayList<String> list = new ArrayList<String>(availableQuestions);
			int rnd = (int)Math.round(Math.random()*availableQuestions.size());
			if (rnd<0) rnd=0;
			if (rnd>=availableQuestions.size()) rnd = availableQuestions.size()-1;
			String mid = list.get(rnd);
			
			try {
				nextURL = "/multChoice.jsp?" 
						+ "&numoptions=4" 
						+ "&relation=" + URLEncoder.encode(relation, "UTF-8")  
						+ "&mid=" + URLEncoder.encode(mid, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return nextURL;
			
		}
	}

	
}
