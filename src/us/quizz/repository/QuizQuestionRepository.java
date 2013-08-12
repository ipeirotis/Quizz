package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.GoldAnswer;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.PMF;

public class QuizQuestionRepository {

	public static QuizQuestion getQuizQuestion(String quizid, String mid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		QuizQuestion quiz = null;
		try {
			quiz = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(quizid, mid));
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
		return quiz;
	}
	
	public static List<QuizQuestion> getQuizQuestions() {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(QuizQuestion.class);
		List<QuizQuestion> list = new ArrayList<QuizQuestion>();
		int limit = 1000;
		int i=0;
		while (true) {
			query.setRange(i, i+limit);
			List<QuizQuestion> results = (List<QuizQuestion>) query.execute();
			if (results.size()==0) break;
			list.addAll(results);
			i+=limit;
		}
		pm.close();
		
		return list;
	}
	
	public static Set<String> getQuizQuestionsWithGold(String quizid) {
		
		
		String key = "quizquestions_"+quizid;
		Set<String> availableQuestions = CachePMF.get(key, Set.class);
		if (availableQuestions==null) {
			
			PersistenceManager	pm = PMF.get().getPersistenceManager();
			Query query = pm.newQuery(QuizQuestion.class);
			query.setFilter("relation == quizParam && hasGoldAnswer==hasGoldParam");
			query.declareParameters("String quizParam, Boolean hasGoldParam");

			Map<String,Object> params = new HashMap<String, Object>();
			params.put("quizParam", quizid);
			params.put("hasGoldParam", Boolean.TRUE);
			
			@SuppressWarnings("unchecked")
			List<QuizQuestion> questions = (List<QuizQuestion>) query.executeWithMap(params);
			
			availableQuestions = new HashSet<String>();
			for (QuizQuestion q : questions) {
				availableQuestions.add(q.getFreebaseEntityId());
			}
			pm.close();
			
			CachePMF.put(key,availableQuestions);
		}
		
		return availableQuestions;
	}
	
	
	
	public static void storeQuizQuestion(QuizQuestion q) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(q);
		pm.close();
	}
	
	public static ArrayList<String> getGoldAnswers(String quizid, String mid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);
		params.put("midParam", mid);
		@SuppressWarnings("unchecked")
		List<GoldAnswer> qresults = (List<GoldAnswer>) q.executeWithMap(params);
		pm.close();

		ArrayList<String> result = new ArrayList<String>();
		for (GoldAnswer ga : qresults) {
			result.add(ga.getAnswer());
		}

		return result;
	}

	
	private static ArrayList<String> getAllQuizGoldAnswers(String quizid) {
		String key = "getgoldanswers_" + quizid;
		
		@SuppressWarnings("unchecked")
		ArrayList<String> result = CachePMF.get(key, ArrayList.class);
		if (result == null) {
			result = getAllGoldAnswersNoCache(quizid);
			CachePMF.put(key, result);
		}
		return result;
	}

	
	private static ArrayList<String> getAllGoldAnswersNoCache(String quizid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == quizParam");
		q.declareParameters("String quizParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);
		@SuppressWarnings("unchecked")
		List<GoldAnswer> qresults = (List<GoldAnswer>) q.executeWithMap(params);
		pm.close();

		ArrayList<String> result = new ArrayList<String>();
		for (GoldAnswer ga : qresults) {
			result.add(ga.getAnswer());
		}

		return result;
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
		int r = (int) Math.round(Math.random() * gold.size());
		if (r >= gold.size()) {
			r = gold.size() - 1;
		}
		
		result = gold.get(r);
		CachePMF.put(cachekey, result);
		
		return result;
	}
	
	public static List<String> getUserAnswers(String quizid, String mid) {

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);
		params.put("midParam", mid);
		
		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) q.executeWithMap(params);
		pm.close();

		List<String> result = new ArrayList<String>();
		for (UserAnswer ue : answers) {
			// if (ue.getUserid().equals(ignoreUser)) {
			// continue;
			// }
			result.add(ue.getUseranswer());

		}
		return result;

	}
	
	public static Set<String> getIncorrectAnswers(String quizid, String mid, String name, int size) {
		
		String cachekey = "quizquestion-pyrite-"+quizid+mid;
		
		@SuppressWarnings("unchecked")
		Set<String> results = CachePMF.get(cachekey, Set.class);
		if (results != null) return results;
		
		results = new TreeSet<String>();

		// Get a set of potential answers from other questions
		List<String> wrongAnswers = QuizQuestionRepository.getAllQuizGoldAnswers(quizid);
		
		// Remove any self-reference
		wrongAnswers.remove(name);
		
		// Remove all gold answers
		ArrayList<String> gold = getGoldAnswers(quizid, mid);
		wrongAnswers.removeAll(gold);

		// Get a list of potential good answers from KV and remove them
		// from the list of results. We want only clearly incorrect answers
		List<String> good_silver = getSilverAnswers(quizid, mid, true, 0.5);
		wrongAnswers.removeAll(good_silver);

		while (results.size() < size && wrongAnswers.size()>0) {
			int rnd = (int) Math.round(Math.random() * wrongAnswers.size());
			if (rnd >= wrongAnswers.size()) {
				rnd = wrongAnswers.size() - 1;
			}
			String candidate = wrongAnswers.get(rnd);
			wrongAnswers.remove(rnd);
			results.add(candidate);
		}

		CachePMF.put(cachekey, results);
		
		return results;
	}
	
	public static List<String> getSilverAnswers(String quizid, String mid, boolean highprobability, 	double prob_threshold) {

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(SilverAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);
		params.put("midParam", mid);
		
		@SuppressWarnings("unchecked")
		List<SilverAnswer> answers = (List<SilverAnswer>) q.executeWithMap(params);
		
		pm.close();

		List<String> result = new ArrayList<String>();
		for (SilverAnswer sa : answers) {
			if (highprobability) {
				if (sa.getProbability() >= prob_threshold) {
					result.add(sa.getAnswer());
				}
			} else {
				if (sa.getProbability() <= prob_threshold) {
					result.add(sa.getAnswer());
				}
			}
		}
		return result;

	}
	
	public static int getNumberOfUserAnswersExcludingIDK(String quiz, String mid) {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
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
	}
	
	public static int getNumberOfCorrectUserAnswers(String quiz, String mid) {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
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
	}

	
}
