package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.UserAnswer;
import us.quizz.utils.CachePMF;
import us.quizz.utils.Helper;
import us.quizz.utils.PMF;

import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

public class QuizQuestionRepository extends BaseRepository<Question>{
	
	QuizRepository quizRepository;
	
	@Inject
	public QuizQuestionRepository(QuizRepository quizRepository) {
		super(Question.class);
		this.quizRepository = quizRepository;
	}
	
	@Override
	protected Key getKey(Question item) {
		return item.getKey();
	}
	
	public Map<String, Set<Question>> getNextQuizQuestions(String quiz, int n) {
		String key = "getquizquestion_" + quiz + n;
		@SuppressWarnings("unchecked")
		Map<String, Set<Question>> result = CachePMF.get(key, Map.class);
		if (result != null)
			return result;
		else
			result = new HashMap<String, Set<Question>>();

		int N = n * 5;
		ArrayList<Question> goldQuestions = getSomeQuizQuestionsWithGold(quiz, N);
		result.put("gold", Helper.trySelectingRandomElements(goldQuestions, n));
		ArrayList<Question> silverQuestions = getSomeQuizQuestionsWithSilver(quiz, N);
		result.put("silver", Helper.trySelectingRandomElements(silverQuestions, n));
		
		int cached_lifetime = 5 * 60; // 10 minutes
		CachePMF.put(key, result, cached_lifetime);

		return result;
	}

	public Question getQuizQuestion(String questionID) {
		return getQuizQuestion(Long.parseLong(questionID));
	}

	public Question getQuizQuestion(Long questionID) {
		return get(questionID);
	}

	protected Query getQuestionBaseQuery(PersistenceManager pm) {
		Query query = pm.newQuery(Question.class);
		query.getFetchPlan().setFetchSize(1000);
		return query;
	}

	protected Query getQuestionQuery(PersistenceManager pm,
			String filter, String declaredParameters) {
		Query query = getQuestionBaseQuery(pm);
		query.setFilter(filter);
		query.declareParameters(declaredParameters);
		return query;
	}

	public ArrayList<Question> getQuizQuestions() {

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

	protected ArrayList<Question> getQuestions(String filter,
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

	protected ArrayList<Question> getQuestionsWithCaching(String key,
			String filter, String declaredParameters, Map<String, Object> params) {
		@SuppressWarnings("unchecked")
		ArrayList<Question> questions = CachePMF.get(key, ArrayList.class);
		if (questions != null)
			return questions;
		questions = getQuestions(filter, declaredParameters, params);
		CachePMF.put(key, questions);
		return questions;
	}

	public ArrayList<Question> getQuizQuestions(String quizid) {

		String key = "quizquestions_all_" + quizid;
		String filter = "quizID == quizParam";
		String declaredParameters = "String quizParam";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);
		return getQuestionsWithCaching(key, filter, declaredParameters, params);
	}

	protected Query getQuizGoldQuestionsQuery(PersistenceManager pm,
			String quizID) {
		String filter = "quizID == quizParam && hasGoldAnswer==hasGoldParam";
		String declaredParameters = "String quizParam, Boolean hasGoldParam";
		return getQuestionQuery(pm, filter, declaredParameters);
	}

	protected Map<String, Object> getQuizGoldQuestionsParameters(
			String quizID) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizID);
		params.put("hasGoldParam", Boolean.TRUE);
		return params;
	}
	
	protected Query getQuizSilverQuestionsQuery(PersistenceManager pm,
			String quizID) {
		String filter = "quizID == quizParam && hasSilverAnswer==hasSilverParam";
		String declaredParameters = "String quizParam, Boolean hasSilverParam";
		return getQuestionQuery(pm, filter, declaredParameters);
	}
	
	protected Map<String, Object> getQuizSilverQuestionsParameters(
			String quizID) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizID);
		params.put("hasSilverParam", Boolean.TRUE);
		return params;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Question> getSomeQuizQuestionsWithGold(
			String quizID, int amount) {
		PersistenceManager pm = PMF.getPM();

		try {
			Quiz quiz = quizRepository.singleGetObjectById(Quiz.generateKeyFromID(quizID));
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
	
	@SuppressWarnings("unchecked")
	public ArrayList<Question> getSomeQuizQuestionsWithSilver(
			String quizID, int amount) {
		PersistenceManager pm = PMF.getPM();

		try {
			Quiz quiz = quizRepository.get(quizID);
			int questionsWithSilver = quiz.getQuestions() - quiz.getGold();
			Query query = getQuizSilverQuestionsQuery(pm, quizID);
			setRandomRange(query, questionsWithSilver, amount);
			ArrayList<Question> result = new ArrayList<Question>(
					(List<Question>) query
							.executeWithMap(getQuizSilverQuestionsParameters(quizID)));

			return result;

		} finally {
			pm.close();
		}

	}

	public void setRandomRange(Query query, int size, int amount) {
		int lower = (int) (Math.random() * Math.max(0, size - amount));
		int upper = Math.min(size, lower + amount);
		query.setRange(lower, upper); // upper is excluded index
	}

	public ArrayList<Question> getQuizQuestionsWithGold(String quizID) {

		String key = "quizquestions_gold_" + quizID;
		String filter = "quizID == quizParam && hasGoldAnswer==hasGoldParam";
		String declaredParameters = "String quizParam, Boolean hasGoldParam";
		return getQuestionsWithCaching(key, filter, declaredParameters,
				getQuizGoldQuestionsParameters(quizID));
	}
	
	@SuppressWarnings("unchecked")
	public List<Question> getQuizQuestionsByKeys(List<Key> keys) {
		Query q = PMF.getPM().newQuery("select from " + Question.class.getName() + " where key == :keys");
	    return (List<Question>) q.execute(keys);
	}

	public void storeQuizQuestion(Question q) {
		singleMakePersistent(q);
	}

	public void removeWithoutUpdates(Long questionID) {
		PersistenceManager pm = PMF.getPM();
		try {
			Question qq = pm.getObjectById(Question.class, questionID);
			pm.deletePersistent(qq);
		} finally {
			pm.close();
		}
	}

	public List<UserAnswer> getUserAnswers(Question question) {

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

	public int getNumberOfUserAnswersExcludingIDK(String questionID) {
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

	public int getNumberOfCorrectUserAnswers(String questionID) {
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
