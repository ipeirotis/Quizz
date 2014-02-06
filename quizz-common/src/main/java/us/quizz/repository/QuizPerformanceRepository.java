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

import com.google.appengine.api.datastore.Key;

public class QuizPerformanceRepository extends BaseRepository<QuizPerformance>{
	
	public QuizPerformanceRepository() {
		super(QuizPerformance.class);
	}
	
	@Override
	protected Key getKey(QuizPerformance item) {
		return item.getKey();
	}

	public QuizPerformance getQuizPerformance(String quizid,
			String userid) {
		String key = "qp_" + quizid + "_" + userid;
		return PMF.singleGetObjectByIdWithCaching(key, QuizPerformance.class,
				QuizPerformance.generateKeyFromID(quizid, userid));
	}

	protected List<QuizPerformance> getQuizPerformanceFilterOnField(
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
	public long getNumberOfAnswers(String quizID, int a, int b){
		PersistenceManager pm = PMF.getPM();
		Map<String, Object> params = new HashMap<String, Object>();
		Query q = pm.newQuery(QuizPerformance.class);
		
		StringBuilder sb = new StringBuilder();
		if(quizID != null)
			sb.append("quiz == quizIDparam && ");
		
		sb.append("correctanswers <= aParam");
		
		if(quizID != null){
			q.declareParameters("String quizIDparam");
			params.put("quizIDparam", quizID);
		}
		
		params.put("aParam", a);
		q.setFilter(sb.toString());
		q.declareParameters("Integer aParam");
		
		int i = 0;
		int limit = 1000;
		long result = 0;
		
		while (true) {
			q.setRange(i, i + limit);
			List<QuizPerformance> list = (List<QuizPerformance>) q.executeWithMap(params);
			if (list.size() == 0)
				break;
			
			for(QuizPerformance quizPerformance : list){
				if(quizPerformance.getIncorrectanswers() == null ||
						quizPerformance.getIncorrectanswers() <= b)
					result++;
			}
			i += limit;
		}
		pm.close();

		return result;
	}

	public List<QuizPerformance> getQuizPerformancesByQuiz(String quizid) {
		return getQuizPerformanceFilterOnField("quiz", quizid);
	}

	public List<QuizPerformance> getQuizPerformancesByUser(String userid) {
		return getQuizPerformanceFilterOnField("userid", userid);
	}

	public List<QuizPerformance> getQuizPerformances() {

		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(QuizPerformance.class);
		// query.getFetchPlan().setFetchSize(500);
		@SuppressWarnings("unchecked")
		List<QuizPerformance> list = (List<QuizPerformance>) query.execute();
		list = new LinkedList<QuizPerformance>(list);
		pm.close();
		return list;
	}

	public void storeQuizPerformance(QuizPerformance qp) {
		cacheQuizPerformance(qp);
		PMF.singleMakePersistent(qp);
	}

	public void cacheQuizPerformance(QuizPerformance qp) {
		String key = "qp_" + qp.getQuiz() + "_" + qp.getUserid();
		CachePMF.put(key, qp);
	}

	public void deleteQuizPerformance(String quizid, String userid) {
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