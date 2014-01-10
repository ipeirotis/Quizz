package us.quizz.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.utils.CachePMF;
import us.quizz.utils.PMF;


public class UserAnswerRepository {
	/**
	 * @return
	 */
	public static List<UserAnswer> getUserAnswers(String quiz, String userid) {

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
	
	public static UserAnswer getUserAnswer(String questionID, String userID) {

		String key = "useranswer_" + questionID + userID;
		return PMF.singleGetObjectByIdWithCaching(key, UserAnswer.class,
				UserAnswer.generateKeyFromID(questionID, userID));
	}
	
	public static UserAnswerFeedback getUserAnswerFeedback(Long questionID, String userID) {

		String key = "useranswerfeedback_"+questionID + userID;
		return PMF.singleGetObjectByIdWithCaching(key, UserAnswerFeedback.class,
				UserAnswerFeedback.generateKeyFromID(questionID, userID));
	}

	public static void storeUserAnswer(UserAnswer ua) {
		PMF.singleMakePersistent(ua);
	}

	public static void storeUserAnswerFeedback(UserAnswerFeedback uaf) {
		String key = "useranswerfeedback_"+uaf.getQuestionID() + uaf.getUserid();
		CachePMF.put(key, uaf);
		PMF.singleMakePersistent(uaf);
	}
}