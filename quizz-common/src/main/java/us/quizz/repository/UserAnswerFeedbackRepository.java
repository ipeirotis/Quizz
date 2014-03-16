package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.UserAnswerFeedback;

public class UserAnswerFeedbackRepository extends BaseRepository<UserAnswerFeedback> {
  public UserAnswerFeedbackRepository() {
    super(UserAnswerFeedback.class);
  }

  @Override
  protected Key getKey(UserAnswerFeedback item) {
    return item.getKey();
  }
}
