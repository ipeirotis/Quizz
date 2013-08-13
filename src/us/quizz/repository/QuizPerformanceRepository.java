package us.quizz.repository;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.ipeirotis.crowdquiz.entities.QuizPerformance;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.PMF;

public class QuizPerformanceRepository {

	public static QuizPerformance getQuizPerformance(String quizid, String userid) {
		String key = "qp_"+quizid+"_"+userid;
		QuizPerformance qp = CachePMF.get(key, QuizPerformance.class);
		if (qp!=null) return qp;
	
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			qp = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(quizid, userid));
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
		CachePMF.put(key, qp);
		return qp;
	} 
	
	public static List<QuizPerformance> getQuizPerformances(String quizid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(QuizPerformance.class);
		q.setFilter("quiz == quizParam");
		q.declareParameters("String quizParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);

		@SuppressWarnings("unchecked")
		List<QuizPerformance> results = (List<QuizPerformance>) q.executeWithMap(params);
		pm.close();
		
		return results;
	}
	
	public static void storeQuizPerformance(QuizPerformance qp) {
		String key = "qp_"+qp.getQuiz()+"_"+qp.getUserid();
		CachePMF.put(key, qp);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(qp);
		pm.close();
	} 
	
	public static void cacheQuizPerformance(QuizPerformance qp) {
		String key = "qp_"+qp.getQuiz()+"_"+qp.getUserid();
		CachePMF.put(key, qp);
	} 
	
	public static void deleteQuizPerformance(String quizid, String userid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			QuizPerformance qp = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(quizid, userid));
			pm.deletePersistent(qp);
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
	} 
	
}