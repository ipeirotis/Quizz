package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.UserAnswerFeedback;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

public class UserAnswerFeedbackRepository extends BaseRepository<UserAnswerFeedback> {
  public UserAnswerFeedbackRepository() {
    super(UserAnswerFeedback.class);
  }

  @Override
  protected Key getKey(UserAnswerFeedback item) {
    return item.getKey();
  }

  public UserAnswerFeedback getUserAnswerFeedback(Long questionID, String userid) {
    return singleGetObjectByIdThrowing(
        UserAnswerFeedback.generateKeyFromID(questionID, userid));
  }

  public void storeUserAnswerFeedback(UserAnswerFeedback uaf) {
    String key = MemcacheKey.getUserAnswerFeedback(uaf.getQuestionID(), uaf.getUserid());
    CachePMF.put(key, uaf);
    singleMakePersistent(uaf);
  }
}
