package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.UserAnswer;
import us.quizz.repository.UserAnswerRepository;

import java.util.List;
import java.util.Set;

public class UserAnswerService {
  private UserAnswerRepository userAnswerRepository;

  @Inject
  public UserAnswerService(UserAnswerRepository userAnswerRepository){
    this.userAnswerRepository = userAnswerRepository;
  }

  public List<UserAnswer> list(String quizID) {
    return userAnswerRepository.list(quizID);
  }

  public UserAnswer save(UserAnswer userAnswer) {
    return userAnswerRepository.saveAndGet(userAnswer);
  }

  public UserAnswer get(Long id) {
    return userAnswerRepository.get(id);
  }

  public void remove(List<UserAnswer> answers){
    userAnswerRepository.delete(answers);
  }

  public List<UserAnswer> getUserAnswers(String quiz, String userid) {
    return userAnswerRepository.getUserAnswers(quiz, userid);
  }

  public List<UserAnswer> getUserAnswersForQuestion(Long questionID) {
    return userAnswerRepository.getUserAnswersForQuestion(questionID);
  }

  public List<UserAnswer> getUserAnswersForQuiz(String quizID) {
    return userAnswerRepository.getUserAnswersForQuiz(quizID);
  }

  public int getNumberOfUserAnswersExcludingIDK(Long questionID) {
    return userAnswerRepository.getNumberOfUserAnswersExcludingIDK(questionID);
  }

  public int getNumberOfCorrectUserAnswers(Long questionID) {
    return userAnswerRepository.getNumberOfCorrectUserAnswers(questionID);
  }

  public Set<String> getUserIDs(String quizID) {
    return userAnswerRepository.getUserIDs(quizID);
  }

  public Integer getNumberOfUserAnswers(String quizID) {
    return userAnswerRepository.getNumberOfUserAnswers(quizID);
  }
}
