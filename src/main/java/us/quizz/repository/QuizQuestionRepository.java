package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.GoldAnswer;
import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.QuizQuestionInstance;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.Helper;
import com.ipeirotis.crowdquiz.utils.PMF;

public class QuizQuestionRepository {

	public static QuizQuestion getQuizQuestion(String quizid, String mid) {
		
		String key = "quizquestion_"+quizid+mid;
		return PMF.singleGetObjectByIdWithCaching(key, QuizQuestion.class,
				QuizQuestion.generateKeyFromID(quizid, mid));
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
	
	public static ArrayList<String> getQuizQuestionsWithGold(String quizid) {
		
		String key = "quizquestions_"+quizid;
		@SuppressWarnings("unchecked")
		ArrayList<String> availableQuestions = CachePMF.get(key, ArrayList.class);
		if (availableQuestions!=null) return availableQuestions;
		
		PersistenceManager	pm = PMF.getPM();
		try {
			Query query = pm.newQuery(QuizQuestion.class);
			query.setFilter("relation == quizParam && hasGoldAnswer==hasGoldParam");
			query.declareParameters("String quizParam, Boolean hasGoldParam");
	
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizid);
			params.put("hasGoldParam", Boolean.TRUE);
			
			query.setResult("freebaseEntityId");
			@SuppressWarnings("unchecked")
			ArrayList<String> questions = new ArrayList<String>((List<String>) query.executeWithMap(params));

			CachePMF.put(key, questions);
			return questions;
		} finally {
			pm.close();
		}
	}
	
	public static void storeQuizQuestion(QuizQuestion q) {
		PMF.singleMakePersistent(q);
	}
	
	public static void removeWithoutUpdates(String quizid, String mid) {
		PersistenceManager pm = PMF.getPM();
		try {
			QuizQuestion qq = pm.getObjectById(QuizQuestion.class,
					QuizQuestion.generateKeyFromID(quizid, mid));
			pm.deletePersistent(qq);
		} finally {
			pm.close();
		}
	}
	
	public static ArrayList<String> getGoldAnswers(String quizid, String mid) {
		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(GoldAnswer.class);
			q.setFilter("relation == quizParam && mid == midParam");
			q.declareParameters("String quizParam, String midParam");
	
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizid);
			params.put("midParam", mid);
			
			q.setResult("answer");
			@SuppressWarnings("unchecked")
			List<String> qresult = (List<String>) q.executeWithMap(params);
			ArrayList<String> result = new ArrayList<String>(qresult);
			return result;
		} finally {
			pm.close();
		}
	}

	public static String getRandomGoldAnswer(String quizid, String mid) {
		
		String cachekey = "quizquestion-gold-"+quizid+mid;
		String result = CachePMF.get(cachekey, String.class);
		if (result != null) return result;
		
		// First we need to put one correct result
		ArrayList<String> gold = getGoldAnswers(quizid, mid);
		if (gold.size() == 0) {
			return null;
		}

		// Select one gold answer at random and put it in the results
		int r = (int) (Math.random() * gold.size());

		result = gold.get(r);
		CachePMF.put(cachekey, result);
		
		return result;
	}
	
	public static QuizQuestionInstance getQuizQuestionInstanceWithGold(String quiz, String mid, String name, int answers) {
		String key = "qqi_"+quiz+mid;
		QuizQuestionInstance result = CachePMF.get(key, QuizQuestionInstance.class);
		if (result != null) return result;
		
		Set<String> choices =  QuizQuestionRepository.getIncorrectAnswers(quiz, mid, name, answers-1);
		String gold = QuizQuestionRepository.getRandomGoldAnswer(quiz, mid);
		choices.add(gold);
		
		result = new QuizQuestionInstance(quiz, mid, choices, gold, true);
		CachePMF.put(key, result);
		return result;
	}
	
	public static List<String> getUserAnswers(String quizid, String mid) {

		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("relation == quizParam && mid == midParam");
			q.declareParameters("String quizParam, String midParam");
	
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizid);
			params.put("midParam", mid);
			
			q.setResult("useranswer");
			
			@SuppressWarnings("unchecked")
			List<String> result = (List<String>) q.executeWithMap(params);
			return result;
		} finally {
			pm.close();
		}
	}
	
	protected static ArrayList<String> getSomeQuizGoldAnswers(String quizId, int number) {
		Quiz quiz = QuizRepository.getQuiz(quizId); 
		
		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(GoldAnswer.class);
					
			q.setFilter("relation == quizParam");
			q.declareParameters("String quizParam");
	
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizId);
			
			q.setResult("answer");
			int fstIdx = (int) (Math.random() * Math.max(0, quiz.getGold() - number));
			q.setRange(fstIdx, Math.min(quiz.getGold(), fstIdx + number));
	
			@SuppressWarnings("unchecked")
			ArrayList<String> result = new ArrayList<String>((List<String>) q.executeWithMap(params));
			
			return result;
		} finally {
			pm.close();
		}
	}
	
	public static Set<String> getIncorrectAnswers(String quizid, String mid, String name, int size) {
		
		String cachekey = "quizquestion-pyrite-"+quizid+mid;
		
		@SuppressWarnings("unchecked")
		Set<String> results = CachePMF.get(cachekey, Set.class);
		if (results != null) return results;
		
		// Get a set of potential answers from other questions
		ArrayList<String> wrongAnswers = getSomeQuizGoldAnswers(quizid, 500);
		
		// Remove any self-reference
		wrongAnswers.remove(name);
		
		// Remove all gold answers
		ArrayList<String> gold = getGoldAnswers(quizid, mid);
		wrongAnswers.removeAll(gold);

		// Get a list of potential good answers from KV and remove them
		// from the list of results. We want only clearly incorrect answers
		List<String> good_silver = getSilverAnswers(quizid, mid, true, 0.5);
		wrongAnswers.removeAll(good_silver);

		results = Helper.selectRandomElements(wrongAnswers, size);
		CachePMF.put(cachekey, results);
		
		return results;
	}
	
	public static List<String> getSilverAnswers(String quizid, String mid, boolean highprobability, double prob_threshold) {

		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(SilverAnswer.class);
			q.setFilter("relation == quizParam && mid == midParam && probability " + ((highprobability)? ">=" : "<=") + " " + prob_threshold);
			q.declareParameters("String quizParam, String midParam");
	
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizid);
			params.put("midParam", mid);
			
			q.setResult("answer");
			
			@SuppressWarnings("unchecked")
			List<String> answers = (List<String>) q.executeWithMap(params);
			
			return answers;
		} finally {
			pm.close();
		}
	}
	
	public static int getNumberOfUserAnswersExcludingIDK(String quiz, String mid) {
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("relation == quizParam && mid == midParam && action==submitParam");
			q.declareParameters("String quizParam, String midParam, String submitParam");
	
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("quizParam", quiz);
			params.put("midParam", mid);
			params.put("submitParam", "Submit");
	  
			@SuppressWarnings("unchecked")
			List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
			return results.size();
		} finally {
			pm.close();
		}
	}
	
	public static int getNumberOfCorrectUserAnswers(String quiz, String mid) {
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("relation == quizParam && mid == midParam && action==submitParam && isCorrect==correctParam");
			q.declareParameters("String quizParam, String midParam, String submitParam, Boolean correctParam");
	
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("quizParam", quiz);
			params.put("midParam", mid);
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