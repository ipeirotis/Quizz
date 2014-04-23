package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.UserAnswer;
import us.quizz.entities.UserAnswerFeedback;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class UserAnswerRepository extends BaseRepository<UserAnswer> {
  public UserAnswerRepository() {
    super(UserAnswer.class);
  }

  @Override
  protected Key getKey(UserAnswer item) {
    return item.getKey();
  }

  public List<UserAnswer> getUserAnswers(String quizID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("quizID == quizParam");
      q.declareParameters("String quizParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quizID);

      return fetchAllResults(q, params);
    } finally {
      pm.close();
    }
  }

  public List<UserAnswer> getUserAnswers(String quiz, String userid) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("quizID == quizParam && userid == useridParam");
      q.declareParameters("String quizParam, String useridParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quiz);
      params.put("useridParam", userid);

      return fetchAllResults(q, params);
    } finally {
      pm.close();
    }
  }
 
  public List<UserAnswer> getUserAnswersForQuestion(Long questionID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("questionID == questionIDParam");
      q.declareParameters("Long questionIDParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", questionID);

      return fetchAllResults(q, params);
    } finally {
      pm.close();
    }
  }

  @SuppressWarnings("unchecked")
  public List<UserAnswer> getUserAnswersWithChallenge(String quiz, String userid) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("quizID == quizParam && userid == useridParam");
      q.declareParameters("String quizParam, String useridParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quiz);
      params.put("useridParam", userid);

      return fetchAllResults(q, params);
    } finally {
      pm.close();
    }
  }

  public int getNumberOfUserAnswersExcludingIDK(String questionID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("questionID == questionIDParam && action == submitParam");
      q.declareParameters("Long questionIDParam, String submitParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", Long.parseLong(questionID));
      params.put("submitParam", "Submit");

      return countResults(q, params);
    } finally {
      pm.close();
    }
  }

  public int getNumberOfCorrectUserAnswers(String questionID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("questionID == questionIDParam && action == submitParam && " +
                  "isCorrect == correctParam");
      q.declareParameters("Long questionIDParam, String submitParam, Boolean correctParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", Long.parseLong(questionID));
      params.put("submitParam", "Submit");
      params.put("correctParam", Boolean.TRUE);

      return countResults(q, params);
    } finally {
      pm.close();
    }
  }

  public Set<String> getUserIDs(String quizID) {
    List<UserAnswer> results = getUserAnswers(quizID);
    Set<String> answers = new TreeSet<String>();
    for (UserAnswer ua : results) {
      answers.add(ua.getUserid());
    }
    return answers;
  }
}
