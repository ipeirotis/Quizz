package us.quizz.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;


public class UserAnswerRepository {
	/**
	 * @return
	 */
	public static List<UserAnswer> getUserAnswers(String quiz, String userid) {

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam && userid == useridParam");
		q.declareParameters("String quizParam, String useridParam");
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quiz);
		params.put("useridParam", userid);

		@SuppressWarnings("unchecked")
		List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
		pm.close();
		return results;
	}
	
	public static void storeUserAnswer(UserAnswer ua) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ua);
		pm.close();
	}

}
