package us.quizz.repository;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.entities.Answer;
import com.ipeirotis.crowdquiz.entities.Question;
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
	
	protected static <T> void deleteAll(PersistenceManager pm, String quizID, Class<T> itemsClass){
		Query q = pm.newQuery(itemsClass);
		q.setFilter("quizID == quizIDParam");
		q.declareParameters("String quizIDParam");
		List<?> items = (List<?>) q.execute(quizID);
		pm.deletePersistentAll(items);
	}
	
	public static void deleteQuiz(String quizID) {
		Quiz quiz = null;
		PersistenceManager pm = PMF.getPM();
		try {
			quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(quizID));
			pm.deletePersistent(quiz);
			
			Class<?>[] itemsClasses = new Class<?>[]{UserAnswer.class,
					Answer.class, Question.class};
			for (Class<?> cls: itemsClasses) {
				deleteAll(pm, quizID, cls);
			}
		} finally {
			pm.close();
		}
	}
	
	protected static <T> Integer getNumberOf(String keyPrefix, boolean useCache, String quiz, Class<T> queryClass) {
		String key = keyPrefix + "_"+quiz;
		
		if (useCache) {
			Integer result = CachePMF.get(key, Integer.class);
			if (result != null) return result;
		}
		
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = pm.newQuery(queryClass);
			q.setFilter("quizID == quizParam");
			q.declareParameters("String quizParam");
			@SuppressWarnings("unchecked")
			List<T> results = (List<T>) q.execute(quiz);
			Integer result = results.size();
			CachePMF.put(key, result);
			return result;
		} finally {
			pm.close();
		}
	}
	
	public static Integer getNumberOfQuizQuestions(String quiz, boolean usecache) {
		return getNumberOf("numquizquestions", usecache, quiz, Question.class);
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
		count = QuizRepository.getNumberOfUserAnswers(quiz, false);
		q.setSubmitted(count);
		storeQuiz(q);
	}
}
