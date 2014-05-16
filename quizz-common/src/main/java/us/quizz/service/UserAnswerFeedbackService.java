package us.quizz.service;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import us.quizz.entities.UserAnswerFeedback;
import us.quizz.repository.UserAnswerFeedbackRepository;

import java.util.List;

public class UserAnswerFeedbackService {
  private UserAnswerFeedbackRepository userAnswerFeedbackRepository;

  @Inject
  public UserAnswerFeedbackService(UserAnswerFeedbackRepository userAnswerFeedbackRepository){
    this.userAnswerFeedbackRepository = userAnswerFeedbackRepository;
  }

  public UserAnswerFeedback save(UserAnswerFeedback userAnswerFeedback){
    return userAnswerFeedbackRepository.saveAndGet(userAnswerFeedback);
  }

  public List<UserAnswerFeedback> list(){
    return userAnswerFeedbackRepository.list();
  }

  public CollectionResponse<UserAnswerFeedback> listWithCursor(String cursor, Integer limit){
    return userAnswerFeedbackRepository.listWithCursor(cursor, limit);
  }

  public UserAnswerFeedback get(String id){
    return userAnswerFeedbackRepository.get(id);
  }

  public UserAnswerFeedback get(Long questionID, String userid){
    return userAnswerFeedbackRepository.get(questionID + "_" + userid);
  }

  public void delete(String id) {
    userAnswerFeedbackRepository.delete(id);
  }
}
