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
		PersistenceManager pm = PMF.getPM();
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
		PersistenceManager pm = PMF.getPM();
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
	
	protected static <T> Integer getNumberOf(String keyPrefix, boolean useCache, String quiz, Class<T> queryClass) {
		String key = keyPrefix + "_"+quiz;
		
		if (useCache) {
			Integer result = CachePMF.get(key, Integer.class);
			if (result != null) return result;
		}
		
		PersistenceManager	pm = PMF.getPM();
		Query q = pm.newQuery(queryClass);
		q.setFilter("relation == quizParam");
		q.declareParameters("String quizParam");
		@SuppressWarnings("unchecked")
		List<T> results = (List<T>) q.execute(quiz);
		Integer result = results.size();
		CachePMF.put(key, result);
		return result;
	}
	
	public static Integer getNumberOfGoldAnswers(String quiz, boolean usecache) {
		return getNumberOf("goldanswers", usecache, quiz, GoldAnswer.class);
	}
	
	public static Integer getNumberOfQuizQuestions(String quiz, boolean usecache) {
		return getNumberOf("numquizquestions", usecache, quiz, QuizQuestion.class);
	}
	
	public static Integer getNumberOfSilverAnswers(String quiz, boolean usecache) {
		return getNumberOf("silveranswers", usecache, quiz, SilverAnswer.class);
	}
	
	public static Integer getNumberOfUserAnswers(String quiz, boolean usecache) {
		return getNumberOf("quizuseranswers", usecache, quiz, UserAnswer.class);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Quiz> getQuizzes() {
		
		String key = "list_quizzes";
		List<Quiz> quizlist = CachePMF.get(key, List.class);
		if (quizlist != null) return quizlist;
		
		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(Quiz.class);
		quizlist = (List<Quiz>) query.execute();
		pm.close();
		
		CachePMF.put(key, quizlist);
		return quizlist;
	}
}
