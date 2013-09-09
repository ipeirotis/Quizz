package us.quizz.repository;

import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
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
		return PMF.singleGetObjectById(Quiz.class, Quiz.generateKeyFromID(id));
	}
	
	public static void storeQuiz(Quiz q) {
		PMF.singleMakePersistent(q);
	}
	
	protected static <T> void deleteAll(PersistenceManager pm, String id, Class<T> itemsClass){
		Query q = pm.newQuery(itemsClass);
		q.setFilter("relation == relationParam");
		q.declareParameters("String relationParam");
		List<?> items = (List<?>) q.execute(id);
		pm.deletePersistentAll(items);
	}
	
	public static void deleteQuiz(String id) {
		Quiz quiz = null;
		PersistenceManager pm = PMF.getPM();
		try {
			quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(id));
		} catch (JDOObjectNotFoundException e) {
			// TODO: shall we ignore or throw exception about wrong quiz id?
		}
		pm.deletePersistent(quiz);
		
		Class<?>[] itemsClasses = new Class<?>[]{QuizQuestion.class,
				GoldAnswer.class, SilverAnswer.class, UserAnswer.class};
		for (Class<?> cls: itemsClasses) {
			deleteAll(pm, id, cls);
		}
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
	
	public static void updateQuizCounts(String quiz){
		Quiz q = QuizRepository.getQuiz(quiz);
		Integer count = QuizRepository.getNumberOfQuizQuestions(quiz, false);
		q.setQuestions(count);
		count = QuizRepository.getNumberOfGoldAnswers(quiz, false);
		q.setGold(count);
		count = QuizRepository.getNumberOfSilverAnswers(quiz, false);
		q.setSilver(count);
		count = QuizRepository.getNumberOfUserAnswers(quiz, false);
		q.setSubmitted(count);
		storeQuiz(q);
	}
}
