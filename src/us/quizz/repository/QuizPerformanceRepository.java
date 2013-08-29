package us.quizz.repository;


import java.util.ArrayList;
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
	
		PersistenceManager pm = PMF.getPM();
		try {
			qp = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(quizid, userid));
		} catch (Exception e) {
			// qp = new QuizPerformance(quizid, userid);
		} finally {
			pm.close();
		}
		CachePMF.put(key, qp);
		return qp;
	} 
	
	public static List<QuizPerformance> getQuizPerformancesByQuiz(String quizid) {
		PersistenceManager pm = PMF.getPM();

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
	
	public static List<QuizPerformance> getQuizPerformancesByUser(String userid) {
		PersistenceManager pm = PMF.getPM();

		Query q = pm.newQuery(QuizPerformance.class);
		q.setFilter("userid == useridParam");
		q.declareParameters("String useridParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("useridParam", userid);

		@SuppressWarnings("unchecked")
		List<QuizPerformance> results = (List<QuizPerformance>) q.executeWithMap(params);
		pm.close();
		
		return results;
	}
	
	public static List<QuizPerformance> getQuizPerformances() {
		
		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(QuizPerformance.class);
		List<QuizPerformance> list = new ArrayList<QuizPerformance>();
		int limit = 1000;
		int i=0;
		while (true) {
			query.setRange(i, i+limit);
			@SuppressWarnings("unchecked")
			List<QuizPerformance> results = (List<QuizPerformance>) query.execute();
			if (results.size()==0) break;
			list.addAll(results);
			i+=limit;
		}
		pm.close();
		
		return list;
	}
	
	public static void storeQuizPerformance(QuizPerformance qp) {
		cacheQuizPerformance(qp);
		PMF.singleMakePersistent(qp);
	} 
	
	public static void cacheQuizPerformance(QuizPerformance qp) {
		String key = "qp_"+qp.getQuiz()+"_"+qp.getUserid();
		CachePMF.put(key, qp);
	} 
	
	public static void deleteQuizPerformance(String quizid, String userid) {
		PersistenceManager pm = PMF.getPM();
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