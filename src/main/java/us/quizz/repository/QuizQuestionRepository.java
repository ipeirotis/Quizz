package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.UserAnswer;
import us.quizz.utils.CachePMF;
import us.quizz.utils.PMF;

public class QuizQuestionRepository {

	public static Question getQuizQuestion(String questionID) {
		return getQuizQuestion(Long.parseLong(questionID));
	}

	public static Question getQuizQuestion(Long questionID) {
		return PMF.singleGetObjectById(Question.class, questionID);
	}

	protected static Query getQuestionBaseQuery(PersistenceManager pm) {
		Query query = pm.newQuery(Question.class);
		query.getFetchPlan().setFetchSize(1000);
		return query;
	}

	protected static Query getQuestionQuery(PersistenceManager pm,
			String filter, String declaredParameters) {
		Query query = getQuestionBaseQuery(pm);
		query.setFilter(filter);
		query.declareParameters(declaredParameters);
		return query;
	}

	public static ArrayList<Question> getQuizQuestions() {

		PersistenceManager pm = PMF.getPM();
		try {
			Query query = getQuestionBaseQuery(pm);
			@SuppressWarnings("unchecked")
			List<Question> results = (List<Question>) query.execute();
			return new ArrayList<Question>(results);
		} finally {
			pm.close();
		}
	}

	protected static ArrayList<Question> getQuestions(String filter,
			String declaredParameters, Map<String, Object> params) {
		PersistenceManager pm = PMF.getPM();
		try {
			Query query = getQuestionQuery(pm, filter, declaredParameters);

			@SuppressWarnings("unchecked")
			ArrayList<Question> questions = new ArrayList<Question>(
					(List<Question>) query.executeWithMap(params));
			return questions;
		} finally {
			pm.close();
		}
	}

	protected static ArrayList<Question> getQuestionsWithCaching(String key,
			String filter, String declaredParameters, Map<String, Object> params) {
		@SuppressWarnings("unchecked")
		ArrayList<Question> questions = CachePMF.get(key, ArrayList.class);
		if (questions != null)
			return questions;
		questions = getQuestions(filter, declaredParameters, params);
		CachePMF.put(key, questions);
		return questions;
	}

	public static ArrayList<Question> getQuizQuestions(String quizid) {

		String key = "quizquestions_all_" + quizid;
		String filter = "quizID == quizParam";
		String declaredParameters = "String quizParam";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);
		return getQuestionsWithCaching(key, filter, declaredParameters, params);
	}

	protected static Query getQuizGoldQuestionsQuery(PersistenceManager pm,
			String quizID) {
		String filter = "quizID == quizParam && hasGoldAnswer==hasGoldParam";
		String declaredParameters = "String quizParam, Boolean hasGoldParam";
		return getQuestionQuery(pm, filter, declaredParameters);
	}

	protected static Map<String, Object> getQuizGoldQuestionsParameters(
			String quizID) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizID);
		params.put("hasGoldParam", Boolean.TRUE);
		return params;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Question> getSomeQuizQuestionsWithGold(
			String quizID, int amount) {
		PersistenceManager pm = PMF.getPM();

		try {
			Quiz quiz = QuizRepository.getQuiz(quizID);
			int questionsWithGold = quiz.getGold();
			Query query = getQuizGoldQuestionsQuery(pm, quizID);
			setRandomRange(query, questionsWithGold, amount);
			ArrayList<Question> result = new ArrayList<Question>(
					(List<Question>) query
							.executeWithMap(getQuizGoldQuestionsParameters(quizID)));

			return result;

		} finally {
			pm.close();
		}

	}

	public static void setRandomRange(Query query, int size, int amount) {
		int lower = (int) (Math.random() * Math.max(0, size - amount));
		int upper = Math.min(size, lower + amount);
		query.setRange(lower, upper); // upper is excluded index
	}

	public static ArrayList<Question> getQuizQuestionsWithGold(String quizID) {

		String key = "quizquestions_gold_" + quizID;
		String filter = "quizID == quizParam && hasGoldAnswer==hasGoldParam";
		String declaredParameters = "String quizParam, Boolean hasGoldParam";
		return getQuestionsWithCaching(key, filter, declaredParameters,
				getQuizGoldQuestionsParameters(quizID));
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
			q.setFilter("questionID == questionParam");
			q.declareParameters("Long questionParam");

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("questionParam", question.getID());

			@SuppressWarnings("unchecked")
			List<UserAnswer> result = (List<UserAnswer>) q
					.executeWithMap(params);
			return result;
		} finally {
			pm.close();
		}
	}

	public static int getNumberOfUserAnswersExcludingIDK(String questionID) {
		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("questionID == questionIDParam && action==submitParam");
			q.declareParameters("String questionIDParam, String submitParam");

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("questionIDParam", questionID);
			params.put("submitParam", "Submit");

			@SuppressWarnings("unchecked")
			List<UserAnswer> results = (List<UserAnswer>) q
					.executeWithMap(params);
			return results.size();
		} finally {
			pm.close();
		}
	}

	public static int getNumberOfCorrectUserAnswers(String questionID) {
		PersistenceManager pm = PMF.getPM();
		try {
			Query q = pm.newQuery(UserAnswer.class);
			q.setFilter("questionID == questionIDParam && action==submitParam && isCorrect==correctParam");
			q.declareParameters("String questionIDParam, String submitParam, Boolean correctParam");

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("questionIDParam", questionID);
			params.put("submitParam", "Submit");
			params.put("correctParam", Boolean.TRUE);

			@SuppressWarnings("unchecked")
			List<UserAnswer> results = (List<UserAnswer>) q
					.executeWithMap(params);
			return results.size();
		} finally {
			pm.close();
		}
	}

}
