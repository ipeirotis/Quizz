package us.quizz.repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.QuizPerformance;
import us.quizz.utils.CachePMF;
import us.quizz.utils.PMF;

public class QuizPerformanceRepository {

	public static QuizPerformance getQuizPerformance(String quizid,
			String userid) {
		String key = "qp_" + quizid + "_" + userid;
		return PMF.singleGetObjectByIdWithCaching(key, QuizPerformance.class,
				QuizPerformance.generateKeyFromID(quizid, userid));
	}

	protected static List<QuizPerformance> getQuizPerformanceFilterOnField(
			String field, String value) {

		PersistenceManager pm = PMF.getPM();
		Query q = pm.newQuery(QuizPerformance.class);

		String valueName = field + "Param";
		q.setFilter(field + " == " + valueName);
		q.declareParameters("String " + valueName);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(valueName, value);

		@SuppressWarnings("unchecked")
		List<QuizPerformance> results = (List<QuizPerformance>) q
				.executeWithMap(params);
		pm.close();

		return results;
	}
	
	@SuppressWarnings("unchecked")
	public static long getNumberOfAnswers(String quizID, int a, int b){
		PersistenceManager pm = PMF.getPM();
		Query q = pm.newQuery(QuizPerformance.class);
		
		StringBuilder sb = new StringBuilder();
		sb.append("correctanswers <= aParam");
		if(quizID != null)
			sb.append(" && quiz == quizIDparam");

		q.setFilter(sb.toString());
		q.declareParameters("Integer aParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("aParam", a);
		
		if(quizID != null){
			q.declareParameters("String quizIDparam");
			params.put("quizIDparam", quizID);
		}
		
		int i = 0;
		int limit = 1000;
		long result = 0;
		
		while (true) {
			q.setRange(i, i + limit);
			List<QuizPerformance> list = (List<QuizPerformance>) q.executeWithMap(params);
			if (list.size() == 0)
				break;
			
			for(QuizPerformance quizPerformance : list){
				if(quizPerformance.getIncorrectanswers() <= b)
					result++;
			}
			i += limit;
		}
		pm.close();

		return result;
	}

	public static List<QuizPerformance> getQuizPerformancesByQuiz(String quizid) {
		return getQuizPerformanceFilterOnField("quiz", quizid);
	}

	public static List<QuizPerformance> getQuizPerformancesByUser(String userid) {
		return getQuizPerformanceFilterOnField("userid", userid);
	}

	public static List<QuizPerformance> getQuizPerformances() {

		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(QuizPerformance.class);
		// query.getFetchPlan().setFetchSize(500);
		@SuppressWarnings("unchecked")
		List<QuizPerformance> list = (List<QuizPerformance>) query.execute();
		list = new LinkedList<QuizPerformance>(list);
		pm.close();
		return list;
	}

	public static void storeQuizPerformance(QuizPerformance qp) {
		cacheQuizPerformance(qp);
		PMF.singleMakePersistent(qp);
	}

	public static void cacheQuizPerformance(QuizPerformance qp) {
		String key = "qp_" + qp.getQuiz() + "_" + qp.getUserid();
		CachePMF.put(key, qp);
	}

	public static void deleteQuizPerformance(String quizid, String userid) {
		PersistenceManager pm = PMF.getPM();
		try {
			QuizPerformance qp = pm.getObjectById(QuizPerformance.class,
					QuizPerformance.generateKeyFromID(quizid, userid));
			pm.deletePersistent(qp);
		} catch (Exception e) {
			;
		} finally {
			pm.close();
		}
	}

}