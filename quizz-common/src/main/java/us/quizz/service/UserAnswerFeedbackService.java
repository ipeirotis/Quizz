package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.UserAnswerFeedback;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.UserAnswerFeedbackRepository;

public class UserAnswerFeedbackService extends OfyBaseService<UserAnswerFeedback> {
  @Inject
  public UserAnswerFeedbackService(UserAnswerFeedbackRepository userAnswerFeedbackRepository){
    super(userAnswerFeedbackRepository);
  }

  public UserAnswerFeedback get(Long questionID, String userid){
    return get(UserAnswerFeedback.generateId(questionID, userid));
  }
}
