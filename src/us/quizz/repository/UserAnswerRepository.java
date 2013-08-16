package us.quizz.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.entities.UserAnswerFeedback;
import com.ipeirotis.crowdquiz.utils.CachePMF;
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
	
	public static UserAnswer getUserAnswer(String quiz, String mid, String userid) {

		String key = "useranswer_"+quiz+mid+userid;
		UserAnswer answer = CachePMF.get(key, UserAnswer.class);
		if (answer!=null) return answer;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			answer = pm.getObjectById(UserAnswer.class, UserAnswer.generateKeyFromID(userid, quiz, mid));
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
		
		if (answer!=null) CachePMF.put(key, answer);
		
		return answer;

		
	}
	
	public static UserAnswerFeedback getUserAnswerFeedback(String quiz, String mid, String userid) {

		String key = "useranswerfeedback_"+quiz+mid+userid;
		UserAnswerFeedback answer = CachePMF.get(key, UserAnswerFeedback.class);
		if (answer!=null) return answer;
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			answer = pm.getObjectById(UserAnswerFeedback.class, UserAnswerFeedback.generateKeyFromID(userid, quiz, mid));
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
		
		if (answer!=null) CachePMF.put(key, answer);
		
		return answer;

		
	}
	
	
	
	
	
	public static void storeUserAnswer(UserAnswer ua) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ua);
		pm.close();
	}

	public static void storeUserAnswerFeedback(UserAnswerFeedback uaf) {
		String key = "useranswerfeedback_"+uaf.getQuiz()+uaf.getMid()+uaf.getUserid();
		CachePMF.put(key, uaf);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(uaf);
		pm.close();
	}

	
	
}