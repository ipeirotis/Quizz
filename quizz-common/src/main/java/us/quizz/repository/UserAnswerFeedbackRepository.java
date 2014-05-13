package us.quizz.repository;

import us.quizz.entities.UserAnswerFeedback;
import us.quizz.ofy.OfyBaseRepository;

public class UserAnswerFeedbackRepository extends OfyBaseRepository<UserAnswerFeedback> {
  public UserAnswerFeedbackRepository() {
    super(UserAnswerFeedback.class);
  }
}
