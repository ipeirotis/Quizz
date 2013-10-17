package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.PMF;

public class QuizQuestionRepository {

	public static QuizQuestion getQuizQuestion(Long questionID) {
		return PMF.singleGetObjectById(QuizQuestion.class, questionID);
	}
	
	public static QuizQuestion getQuizQuestion(String questionID) {
		return getQuizQuestion(Long.parseLong(questionID));
	}
	
	
	
	public static List<QuizQuestion> getQuizQuestions() {
		
		PersistenceManager pm = PMF.getPM();
		try {
			Query query = pm.newQuery(QuizQuestion.class);
			List<QuizQuestion> list = new ArrayList<QuizQuestion>();
			int limit = 1000;
			int i=0;
			while (true) {
				query.setRange(i, i+limit);
				@SuppressWarnings("unchecked")
				List<QuizQuestion> results = (List<QuizQuestion>) query.execute();
				if (results.size()==0) break;
				list.addAll(results);
				i+=limit;
			}
			return list;
		} finally {
			pm.close();
		}
	}
	
	public static ArrayList<QuizQuestion> getQuizQuestionsWithGold(String quizid) {
		
		String key = "quizquestions_"+quizid;
		@SuppressWarnings("unchecked")
		ArrayList<QuizQuestion> availableQuestions = CachePMF.get(key, ArrayList.class);
		if (availableQuestions!=null) return availableQuestions;
		
		PersistenceManager	pm = PMF.getPM();
		try {
			Query query = pm.newQuery(QuizQuestion.class);
			query.setFilter("relation == quizParam && hasGoldAnswer==hasGoldParam");
			query.declareParameters("String quizParam, Boolean hasGoldParam");
	
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizid);
			params.put("hasGoldParam", Boolean.TRUE);
			
			@SuppressWarnings("unchecked")
			ArrayList<QuizQuestion> questions =
					new ArrayList<QuizQuestion>((List<QuizQuestion>) query.executeWithMap(params));

			CachePMF.put(key, questions);
			return questions;
		} finally {
			pm.close();
		}
	}
	
	public static void storeQuizQuestion(QuizQuestion q) {
		PMF.singleMakePersistent(q);
	}
	
	public static void removeWithoutUpdates(Long questionID) {
		PersistenceManager pm = PMF.getPM();
		try {
			QuizQuestion qq = pm.getObjectById(QuizQuestion.class, questionID);
			pm.deletePersistent(qq);
		} finally {
			pm.close();
		}
	}
	
	public static List<UserAnswer> getUserAnswers(QuizQuestion question) {

		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("quizID == quizParam");
			q.declareParameters("String quizParam");
	
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("quizParam", question.getQuizzID());

			@SuppressWarnings("unchecked")
			List<UserAnswer> result = (List<UserAnswer>) q.executeWithMap(params);
			return result;
		} finally {
			pm.close();
		}
	}

	
	public static int getNumberOfUserAnswersExcludingIDK(Long questionID) {
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("questionID == questionIDParam && action==submitParam");
			q.declareParameters("Long questionIDParam, String submitParam");
	
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("questionIDParam", questionID);
			params.put("submitParam", "Submit");
	  
			@SuppressWarnings("unchecked")
			List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
			return results.size();
		} finally {
			pm.close();
		}
	}
	
	public static int getNumberOfCorrectUserAnswers(Long questionID) {
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("questionID == questionIDParam && action==submitParam && isCorrect==correctParam");
			q.declareParameters("Long questionIDParam, String submitParam, Boolean correctParam");
	
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("questionIDParam", questionID);
			params.put("submitParam", "Submit");
			params.put("correctParam", Boolean.TRUE);
			
			@SuppressWarnings("unchecked")
			List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
			return results.size();
		} finally {
			pm.close();
		}
	}

	
}
