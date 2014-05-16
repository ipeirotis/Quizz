package us.quizz.repository;

import us.quizz.entities.UserAnswer;
import us.quizz.ofy.OfyBaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class UserAnswerRepository extends OfyBaseRepository<UserAnswer> {
  public UserAnswerRepository() {
    super(UserAnswer.class);
  }

  public List<UserAnswer> listByQuizId(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizID);
    return listAll(params);
  }

  public List<UserAnswer> getUserAnswers(String quiz, String userid) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quiz);
    params.put("userid", userid);
    return listAll(params);
  }

  public List<UserAnswer> getUserAnswersForQuestion(Long questionID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("questionID", questionID);
    return listAll(params);
  }
  
  public List<UserAnswer> getUserAnswersForQuiz(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizID);
    return listAll(params);
  }

  public int getNumberOfUserAnswersExcludingIDK(Long questionID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("questionID", questionID);
    params.put("action", "Submit");
    return listAll(params).size();
  }

  public int getNumberOfCorrectUserAnswers(Long questionID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("questionID", questionID);
    params.put("action", "Submit");
    params.put("isCorrect", Boolean.TRUE);
    return listAll(params).size();
  }

  public Set<String> getUserIDs(String quizID) {
    List<UserAnswer> results = listByQuizId(quizID);
    Set<String> answers = new TreeSet<String>();
    for (UserAnswer ua : results) {
      answers.add(ua.getUserid());
    }
    return answers;
  }

  public Integer getNumberOfUserAnswers(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizID", quizID);
    return listAll(params).size();
  }
}
