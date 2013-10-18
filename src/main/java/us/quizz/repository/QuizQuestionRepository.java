package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.PMF;

public class QuizQuestionRepository {

	public static Question getQuizQuestion(String questionID) {
		return getQuizQuestion(Long.parseLong(questionID));
//		return PMF.singleGetObjectById(Question.class, questionID);
	}
	
	public static Question getQuizQuestion(Long questionID) {
		return PMF.singleGetObjectById(Question.class, questionID);
	}
	
	public static List<Question> getQuizQuestions() {
		
		PersistenceManager pm = PMF.getPM();
		try {
			Query query = pm.newQuery(Question.class);
			List<Question> list = new ArrayList<Question>();
			int limit = 1000;
			int i=0;
			while (true) {
				query.setRange(i, i+limit);
				@SuppressWarnings("unchecked")
				List<Question> results = (List<Question>) query.execute();
				if (results.size()==0) break;
				list.addAll(results);
				i+=limit;
			}
			return list;
		} finally {
			pm.close();
		}
	}
	
	public static ArrayList<Question> getQuizQuestionsWithGold(String quizid) {
		
		String key = "quizquestions_"+quizid;
		@SuppressWarnings("unchecked")
		ArrayList<Question> availableQuestions = CachePMF.get(key, ArrayList.class);
		if (availableQuestions!=null) return availableQuestions;
		
		PersistenceManager	pm = PMF.getPM();
		try {
			Query query = pm.newQuery(Question.class);
			query.setFilter("quizID == quizParam && hasGoldAnswer==hasGoldParam");
			query.declareParameters("String quizParam, Boolean hasGoldParam");
	
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizid);
			params.put("hasGoldParam", Boolean.TRUE);
			
			@SuppressWarnings("unchecked")
			ArrayList<Question> questions =
					new ArrayList<Question>((List<Question>) query.executeWithMap(params));

			CachePMF.put(key, questions);
			return questions;
		} finally {
			pm.close();
		}
	}
	
	public static void storeQuizQuestion(Question q) {
		PMF.singleMakePersistent(q);
	}
	
	public static void removeWithoutUpdates(Long questionID) {
		PersistenceManager pm = PMF.getPM();
		try {
			Question qq = pm.getObjectById(Question.class, questionID);
			pm.deletePersistent(qq);
		} finally {
			pm.close();
		}
	}
	
	public static List<UserAnswer> getUserAnswers(Question question) {

		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("quizID == quizParam");
			q.declareParameters("String quizParam");
	
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("quizParam", question.getQuizID());

			@SuppressWarnings("unchecked")
			List<UserAnswer> result = (List<UserAnswer>) q.executeWithMap(params);
			return result;
		} finally {
			pm.close();
		}
	}

	
	public static int getNumberOfUserAnswersExcludingIDK(String questionID) {
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("questionID == questionIDParam && action==submitParam");
			q.declareParameters("String questionIDParam, String submitParam");
	
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
	
	public static int getNumberOfCorrectUserAnswers(String questionID) {
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("questionID == questionIDParam && action==submitParam && isCorrect==correctParam");
			q.declareParameters("String questionIDParam, String submitParam, Boolean correctParam");
	
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
