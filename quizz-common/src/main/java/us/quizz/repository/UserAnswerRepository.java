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

  public List<UserAnswer> getUserAnswers(String quiz) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("quizID == quizParam");
      q.declareParameters("String quizParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quiz);

      List<UserAnswer> answers = new ArrayList<UserAnswer>();
      int limit = 1000;
      int i = 0;
      while (true) {
        q.setRange(i, i + limit);
        @SuppressWarnings("unchecked")
        List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
        if (results.size() == 0) {
          break;
        }
        answers.addAll(results);
        i += limit;
      }
      return answers;
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

      @SuppressWarnings("unchecked")
      List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
      return results;
    } finally {
      pm.close();
    }
  }
  
  public List<UserAnswer> getUsersForQuestion(Long questionID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("questionID == questionIDParam");
      q.declareParameters("Long questionIDParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", questionID);

      @SuppressWarnings("unchecked")
      List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
      return results;
    } finally {
      pm.close();
    }
  }


  
  
  
  public UserAnswer getUserAnswer(String questionID, String userID) {
    String key = MemcacheKey.getUserAnswer(questionID, userID);
    return singleGetObjectByIdWithCaching(key, UserAnswer.class,
        UserAnswer.generateKeyFromID(questionID, userID));
  }

  public UserAnswerFeedback getUserAnswerFeedback(Long questionID, String userID) {
    String key = MemcacheKey.getUserAnswerFeedback(questionID, userID);
    return singleGetObjectByIdWithCaching(key,
        UserAnswerFeedback.class,
        UserAnswerFeedback.generateKeyFromID(questionID, userID));
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

      List<UserAnswer> result = (List<UserAnswer>) q.executeWithMap(params);
      return result;
    } finally {
      pm.close();
    }
  }

  public void storeUserAnswerFeedback(UserAnswerFeedback uaf) {
    String key = MemcacheKey.getUserAnswerFeedback(uaf.getQuestionID(), uaf.getUserid());
    CachePMF.put(key, uaf);
    singleMakePersistent(uaf);
  }
}
