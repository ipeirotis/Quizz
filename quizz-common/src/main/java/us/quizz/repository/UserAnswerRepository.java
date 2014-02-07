package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.utils.CachePMF;
import us.quizz.utils.PMF;

import com.google.appengine.api.datastore.Key;

public class UserAnswerRepository extends BaseRepository<UserAnswer>{
	
	public UserAnswerRepository() {
		super(UserAnswer.class);
	}
	
	@Override
	protected Key getKey(UserAnswer item) {
		return item.getKey();
	}

	public List<UserAnswer> getUserAnswers(String quiz) {

		PersistenceManager pm = PMF.getPM();

		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("quizID == quizParam");
		q.declareParameters("String quizParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);

		List<UserAnswer> answers = new ArrayList<UserAnswer>();
		int limit = 1000;
		int i = 0;
		while (true) {
			q.setRange(i, i + limit);
			@SuppressWarnings("unchecked")
			List<UserAnswer> results = (List<UserAnswer>) q
					.executeWithMap(params);
			if (results.size() == 0)
				break;
			answers.addAll(results);
			i += limit;
		}

		pm.close();
		return answers;
	}

	/**
	 * @return
	 */
	public List<UserAnswer> getUserAnswers(String quiz, String userid) {

		PersistenceManager pm = PMF.getPM();

		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("quizID == quizParam && userid == useridParam");
		q.declareParameters("String quizParam, String useridParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);
		params.put("useridParam", userid);

		@SuppressWarnings("unchecked")
		List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
		pm.close();
		return results;
	}

	public UserAnswer getUserAnswer(String questionID, String userID) {

		String key = "useranswer_" + questionID + userID;
		return PMF.singleGetObjectByIdWithCaching(key, UserAnswer.class,
				UserAnswer.generateKeyFromID(questionID, userID));
	}

	public UserAnswerFeedback getUserAnswerFeedback(Long questionID,
			String userID) {

		String key = "useranswerfeedback_" + questionID + userID;
		return PMF.singleGetObjectByIdWithCaching(key,
				UserAnswerFeedback.class,
				UserAnswerFeedback.generateKeyFromID(questionID, userID));
	}
	
	@SuppressWarnings("unchecked")
	public List<UserAnswer> getUserAnswersWithChallenge(String quiz, String userid) {
		PersistenceManager pm = PMF.getPM();
		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("quizID == quizParam && userid == useridParam");
		q.declareParameters("String quizParam, String useridParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);
		params.put("useridParam", userid);
		
		List<UserAnswer> result = (List<UserAnswer>) q.executeWithMap(params);
		pm.close();
		
		return result;
	}

	public void storeUserAnswerFeedback(UserAnswerFeedback uaf) {
		String key = "useranswerfeedback_" + uaf.getQuestionID()
				+ uaf.getUserid();
		CachePMF.put(key, uaf);
		PMF.singleMakePersistent(uaf);
	}
}