package us.quizz.repository;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import us.quizz.entities.QuizPerformance;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class QuizPerformanceRepository extends BaseRepository<QuizPerformance> {
  public QuizPerformanceRepository() {
    super(QuizPerformance.class);
  }

  @Override
  protected Key getKey(QuizPerformance item) {
    return item.getKey();
  }

  public QuizPerformance getQuizPerformance(String quizid, String userid) {
    String key = MemcacheKey.getQuizPerformanceByUser(quizid, userid);
    return singleGetObjectByIdWithCaching(key, QuizPerformance.class,
       QuizPerformance.generateKeyFromID(quizid, userid));
  }

  protected List<QuizPerformance> getQuizPerformanceFilterOnField(
      String field, String value) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(QuizPerformance.class);
      String valueName = field + "Param";
      q.setFilter(field + " == " + valueName);
      q.declareParameters("String " + valueName);

      Map<String, Object> params = new HashMap<String, Object>();
      params.put(valueName, value);

      return fetchAllResults(q, params);
    } finally {
      pm.close();
    }
  }

  /** 
   * We are calculating the number of users that have at least "a" correct answers
   * and "b" incorrect answers for a given quiz (stats across quizzes if quizID==null)
   */
  public Map<Integer, Map<Integer, Integer>> getCountsForSurvivalProbability(String quizID) {
    List<QuizPerformance> list = getQuizPerformances(quizID);

    Map<Integer, Map<Integer, Integer>> result = new HashMap<Integer, Map<Integer, Integer>>();

    for (QuizPerformance quizPerformance : list) {
      Integer correct = quizPerformance.getCorrectanswers();
      Integer incorrect = quizPerformance.getIncorrectanswers();
      if (correct == null || incorrect == null) continue;
      increaseCounts(result, correct, incorrect);
    }
    return result;
  }

  private void increaseCounts(Map<Integer, Map<Integer, Integer>> result,
      Integer correct, Integer incorrect) {
    for (int a = 0; a <= correct; a++)  {
      Map<Integer, Integer> cntA = result.get(a);
      if (cntA == null) {
        cntA = new HashMap<Integer, Integer>();
        result.put(a, cntA);
      }

      for (int b = 0; b <= incorrect; b++)  {
        Integer cntAB = cntA.get(b);
        if (cntAB == null) {
          cntAB=0;
        }
        cntA.put(b, cntAB + 1);
      }
      result.put(a, cntA);
    }
  }

  public List<QuizPerformance> getQuizPerformancesByQuiz(String quizid) {
    return getQuizPerformanceFilterOnField("quiz", quizid);
  }

  public List<QuizPerformance> getQuizPerformancesByUser(String userid) {
    return getQuizPerformanceFilterOnField("userid", userid);
  }

  public List<QuizPerformance> getQuizPerformances() {
    return getQuizPerformances(null);
  }

  public List<QuizPerformance> getQuizPerformances(String quiz) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(QuizPerformance.class);

      if (quiz != null) {
        q.setFilter("quiz == quizParam");
        q.declareParameters("String quizParam");
      }

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quiz);

      return fetchAllResults(q, params);
    } finally {
      pm.close();
    }
  }

  public void storeQuizPerformance(QuizPerformance qp) {
    cacheQuizPerformance(qp);
    singleMakePersistent(qp);
  }

  public void cacheQuizPerformance(QuizPerformance qp) {
    String key = MemcacheKey.getQuizPerformanceByUser(qp.getQuiz(), qp.getUserid());
    CachePMF.put(key, qp);
  }

  public void deleteQuizPerformance(String quizid, String userid) {
    remove(QuizPerformance.generateKeyFromID(quizid, userid));
  }

  @SuppressWarnings("unchecked")
  public double getScoreSumByIds(Set<Key> ids) {
    if (ids.size() == 0) {
      return 0d;
    }

    PersistenceManager mgr = getPersistenceManager();
    double result = 0d;
    Query q = mgr.newQuery(
        "select from " + QuizPerformance.class.getName() + " where key == :keys");

    List<Key> list = new ArrayList<Key>(ids);
    try {
      for (int i = 0; i < list.size(); i += 1000) {
        List<Key> sublist = list.subList(i, Math.min(i + 1000, list.size()));
        List<QuizPerformance> results = (List<QuizPerformance>) q.execute(sublist);
        for (QuizPerformance qp : results) {
          result += qp.getScore();
        }
      }
    } finally {
      mgr.close();
    }
    return result;
  }
}
