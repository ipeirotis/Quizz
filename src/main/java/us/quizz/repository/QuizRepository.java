package us.quizz.repository;

import java.text.NumberFormat;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.utils.CachePMF;
import us.quizz.utils.PMF;

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
			q.getFetchPlan().setFetchSize(1000);
			q.setResult("quizID");
			List<?> results = (List<?>) q.execute(quiz);
			Integer result = results.size();
			CachePMF.put(key, result);
			return result;
		} finally {
			pm.close();
		}
	}
	

	public static Integer getNumberOfGoldQuestions(String quizID, boolean useCache){
		String key = "goldQuestions_"+quizID;
		
		if (useCache) {
			Integer result = CachePMF.get(key, Integer.class);
			if (result != null) return result;
		}
		
		PersistenceManager	pm = PMF.getPM();
		try {
			Query q = QuizQuestionRepository.getQuizGoldQuestionsQuery(pm, quizID);
			q.setResult("quizID");
			List<?> results = (List<?>) q.executeWithMap(
					QuizQuestionRepository.getQuizGoldQuestionsParameters(quizID));
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
		
		count = QuizRepository.getNumberOfGoldQuestions(quiz, false);
		q.setGold(count);
		
		count = UserReferralRepository.getUserIDsByQuiz(q.getQuizID()).size();
		q.setTotalUsers(count+1); // +1 for smoothing, ensuring no division by 0
		
		List<QuizPerformance> perf = QuizPerformanceRepository.getQuizPerformancesByQuiz(q.getQuizID());
		
		int contributingUsers = perf.size();
		q.setContributingUsers(contributingUsers+1); // +1 for smoothing, ensuring no division by 0
		q.setConversionRate(1.0*q.getContributingUsers()/q.getTotalUsers());
		
		int totalCorrect = 1; // +1 for smoothing, ensuring no division by 0
		int totalAnswers = 1; // +1 for smoothing, ensuring no division by 0
		double bits = 0;
		double avgCorrectness = 0;

		for (QuizPerformance qp: perf) {
			totalCorrect += qp.getCorrectanswers();
			totalAnswers += qp.getTotalanswers();
			avgCorrectness += qp.getPercentageCorrect();
			bits += qp.getScore();
		}
		q.setCorrectAnswers(totalCorrect);
		q.setTotalAnswers(totalAnswers);
		
		q.setCapacity(bits/q.getContributingUsers());
		
		
		
		q.setAvgUserCorrectness(avgCorrectness/q.getContributingUsers());
		q.setAvgAnswerCorrectness(1.0*q.getCorrectAnswers()/q.getTotalAnswers());
		
		storeQuiz(q);
	}
}
