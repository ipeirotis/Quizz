package us.quizz.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import us.quizz.entities.QuizPerformance;
import us.quizz.utils.CachePMF;

import com.google.appengine.api.datastore.Key;

public class QuizPerformanceRepository extends BaseRepository<QuizPerformance> {
  public QuizPerformanceRepository() {
    super(QuizPerformance.class);
  }

  @Override
  protected Key getKey(QuizPerformance item) {
    return item.getKey();
  }

  public QuizPerformance getQuizPerformance(String quizid, String userid) {
    String key = "qp_" + quizid + "_" + userid;
    return singleGetObjectByIdWithCaching(key, QuizPerformance.class,
       QuizPerformance.generateKeyFromID(quizid, userid));
  }

  protected List<QuizPerformance> getQuizPerformanceFilterOnField(
      String field, String value) {

    PersistenceManager pm = getPersistenceManager();
    Query q = pm.newQuery(QuizPerformance.class);

    String valueName = field + "Param";
    q.setFilter(field + " == " + valueName);
    q.declareParameters("String " + valueName);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put(valueName, value);

    @SuppressWarnings("unchecked")
    List<QuizPerformance> results = (List<QuizPerformance>) q.executeWithMap(params);
    pm.close();

    return results;
  }

  @SuppressWarnings("unchecked")
  public long getNumberOfAnswers(String quizID, int a, int b) {
    PersistenceManager pm = getPersistenceManager();
    Map<String, Object> params = new HashMap<String, Object>();
    Query q = pm.newQuery(QuizPerformance.class);

    if(quizID != null) {
      q.setFilter("quiz == quizIDparam && correctanswers >= aParam");
      q.declareParameters("String quizIDparam, Integer aParam");
      params.put("quizIDparam", quizID);
      params.put("aParam", a);
    } else {
      q.setFilter("correctanswers >= aParam");
      q.declareParameters("Integer aParam");
      params.put("aParam", a);
    }

    int i = 0;
    int limit = 1000;
    long result = 0;

    while (true) {
      q.setRange(i, i + limit);
      List<QuizPerformance> list = (List<QuizPerformance>) q.executeWithMap(params);
      if (list.size() == 0) {
        break;
      }
      for(QuizPerformance quizPerformance : list) {
        if(quizPerformance.getIncorrectanswers() == null ||
           quizPerformance.getIncorrectanswers() >= b) {
          result++;
        }
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
    PersistenceManager pm = getPersistenceManager();
    Query query = pm.newQuery(QuizPerformance.class);
    // query.getFetchPlan().setFetchSize(500);
    @SuppressWarnings("unchecked")
    List<QuizPerformance> list = (List<QuizPerformance>) query.execute();
    list = new LinkedList<QuizPerformance>(list);
    pm.close();
    return list;
  }

  public List<QuizPerformance> getQuizPerformances(String quiz) {
    PersistenceManager pm = getPersistenceManager();

    Query q = pm.newQuery(QuizPerformance.class);
    q.setFilter("quiz == quizParam");
    q.declareParameters("String quizParam");

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizParam", quiz);

    List<QuizPerformance> quizPerfomances = new ArrayList<QuizPerformance>();
    int limit = 1000;
    int i = 0;
    while (true) {
      q.setRange(i, i + limit);
      @SuppressWarnings("unchecked")
      List<QuizPerformance> results = (List<QuizPerformance>) q.executeWithMap(params);
      if (results.size() == 0) {
        break;
      }
      quizPerfomances.addAll(results);
      i += limit;
    }

    pm.close();
    return quizPerfomances;
  }


  public void storeQuizPerformance(QuizPerformance qp) {
    cacheQuizPerformance(qp);
    singleMakePersistent(qp);
  }

  public void cacheQuizPerformance(QuizPerformance qp) {
    String key = "qp_" + qp.getQuiz() + "_" + qp.getUserid();
    CachePMF.put(key, qp);
  }

  public void deleteQuizPerformance(String quizid, String userid) {
    PersistenceManager pm = getPersistenceManager();
    try {
      QuizPerformance qp = pm.getObjectById(QuizPerformance.class,
          QuizPerformance.generateKeyFromID(quizid, userid));
      pm.deletePersistent(qp);
    } catch (Exception e) {
    } finally {
      pm.close();
    }
  }

  @SuppressWarnings("unchecked")
  public double getScoreSumByIds(Set<Key> ids) {
    if (ids.size() == 0) {
      return 0d;
    }

    PersistenceManager mgr = null;
    double result = 0d;
    Query q = getPersistenceManager().newQuery(
        "select from " + QuizPerformance.class.getName() + " where key == :keys");

    List<Key> list = new ArrayList<Key>(ids);

    try {
      mgr = getPersistenceManager();
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
