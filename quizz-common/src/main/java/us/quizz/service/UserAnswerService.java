package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.UserAnswer;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.UserAnswerRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserAnswerService extends OfyBaseService<UserAnswer> {
  @Inject
  public UserAnswerService(UserAnswerRepository userAnswerRepository){
    super(userAnswerRepository);
  }

  public List<UserAnswer> list(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    if (quizID != null) {
      params.put("quizID", quizID);
    }
    return listAll(params);
  }

  public List<UserAnswer> getUserAnswers(String quiz, String userid) {
    return ((UserAnswerRepository) baseRepository).getUserAnswers(quiz, userid);
  }

  public List<UserAnswer> getUserAnswersForQuestion(Long questionID) {
    return ((UserAnswerRepository) baseRepository).getUserAnswersForQuestion(questionID);
  }

  public List<UserAnswer> getSubmittedUserAnswersForQuestion(Long questionID) {
    return ((UserAnswerRepository) baseRepository).getSubmittedUserAnswersForQuestion(questionID);
  }

  public List<UserAnswer> getUserAnswersForQuiz(String quizID) {
    return ((UserAnswerRepository) baseRepository).getUserAnswersForQuiz(quizID);
  }

  public int getNumberOfUserAnswersExcludingIDK(Long questionID) {
    return ((UserAnswerRepository) baseRepository).getNumberOfUserAnswersExcludingIDK(questionID);
  }

  public int getNumberOfCorrectUserAnswers(Long questionID) {
    return ((UserAnswerRepository) baseRepository).getNumberOfCorrectUserAnswers(questionID);
  }

  public Set<String> getUserIDs(String quizID) {
    return ((UserAnswerRepository) baseRepository).getUserIDs(quizID);
  }

  public Integer getNumberOfUserAnswers(String quizID) {
    return ((UserAnswerRepository) baseRepository).getNumberOfUserAnswers(quizID);
  }
}
