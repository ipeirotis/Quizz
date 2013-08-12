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
		QuizPerformance qp = null;
	
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			qp = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(quizid, userid));
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
		
		return qp;
	} 
	
	public static List<QuizPerformance> getQuizPerformances(String quizid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(QuizPerformance.class);
		q.setFilter("quiz == quizParam");
		q.declareParameters("String quizParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", quizid);

		List<QuizPerformance> results = (List<QuizPerformance>) q.executeWithMap(params);
		pm.close();
		
		return results;
	}
	
	public static void storeQuizPerformance(QuizPerformance qp) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(qp);
		CachePMF.put("qp_"+qp.getUserid()+"_"+qp.getQuiz(), qp);
		pm.close();
	} 
	
	public static void deleteQuizPerformance(QuizPerformance qp) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.deletePersistent(qp);
		pm.close();
	} 
	
}