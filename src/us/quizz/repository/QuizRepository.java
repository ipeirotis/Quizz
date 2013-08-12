package us.quizz.repository;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.GoldAnswer;
import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.PMF;

public class QuizRepository {

	public static Quiz getQuiz(String id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Quiz quiz = null;
		try {
			quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(id));
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
		return quiz;
	}
	
	public static void storeQuiz(Quiz q) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(q);
		pm.close();
	}
	
	public static void deleteQuiz(String id) {
		Quiz quiz = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(id));
		} catch (Exception e) {

		}
		pm.deletePersistent(quiz);
		
		Query q = pm.newQuery(QuizQuestion.class);
		q.setFilter("relation == relationParam");
		q.declareParameters("String relationParam");
		@SuppressWarnings("unchecked")
		List<QuizQuestion> questions = (List<QuizQuestion>) q.execute(id);
		pm.deletePersistentAll(questions);
		
		q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == relationParam");
		q.declareParameters("String relationParam");
		@SuppressWarnings("unchecked")
		List<GoldAnswer> gold = (List<GoldAnswer>) q.execute(id);
		pm.deletePersistentAll(gold);
		
		
		q = pm.newQuery(SilverAnswer.class);
		q.setFilter("relation == relationParam");
		q.declareParameters("String relationParam");
		@SuppressWarnings("unchecked")
		List<SilverAnswer> silver = (List<SilverAnswer>) q.execute(id);
		pm.deletePersistentAll(silver);
		
		q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == relationParam");
		q.declareParameters("String relationParam");
		@SuppressWarnings("unchecked")
		List<UserAnswer> useranswers = (List<UserAnswer>) q.execute(id);
		pm.deletePersistentAll(useranswers);
		
		pm.close();
	}
	
	public static Integer getNumberOfGoldAnswers(String quiz, boolean usecache) {
		
		String key = "goldanswers_"+quiz;
		
		if (usecache) {
			Integer result = CachePMF.get(key, Integer.class);
			if (result != null) return result;
		}
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == quizParam");
		q.declareParameters("String quizParam");
		@SuppressWarnings("unchecked")
		List<GoldAnswer> results = (List<GoldAnswer>) q.execute(quiz);
		Integer result = results.size();
		CachePMF.put(key, result);
		return result;
	}
	
	public static Integer getNumberOfQuizQuestions(String quiz, boolean usecache) {
		
		String key = "numquizquestions_"+quiz;
		
		if (usecache) {
			Integer result = CachePMF.get(key, Integer.class);
			if (result != null) return result;
		}
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(QuizQuestion.class);
		q.setFilter("relation == quizParam");
		q.declareParameters("String quizParam");
		@SuppressWarnings("unchecked")
		List<QuizQuestion> results = (List<QuizQuestion>) q.execute(quiz);
		Integer result = results.size();
		CachePMF.put(key, result);
		return result;
	}
	
	public static Integer getNumberOfSilverAnswers(String quiz, boolean usecache) {
		
		String key = "silveranswers_"+quiz;
		
		if (usecache) {
			Integer result = CachePMF.get(key, Integer.class);
			if (result != null) return result;
		}
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(SilverAnswer.class);
		q.setFilter("relation == quizParam");
		q.declareParameters("String quizParam");
		@SuppressWarnings("unchecked")
		List<SilverAnswer> results = (List<SilverAnswer>) q.execute(quiz);
		Integer result = results.size();
		CachePMF.put(key, result);
		return result;
	}
	
	public static Integer getNumberOfUserAnswers(String quiz, boolean usecache) {
		
		String key = "quizuseranswers_"+quiz;
		
		if (usecache) {
			Integer result = CachePMF.get(key, Integer.class);
			if (result != null) return result;
		}
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam");
		q.declareParameters("String quizParam");
		@SuppressWarnings("unchecked")
		List<UserAnswer> results = (List<UserAnswer>) q.execute(quiz);
		Integer result = results.size();
		CachePMF.put(key, result);
		return result;
	}
	
	
	
	
	public static List<Quiz> getQuizzes() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery(Quiz.class);
		List<Quiz> quizlist = (List<Quiz>) query.execute();
		pm.close();
		
		return quizlist;
	}
}
