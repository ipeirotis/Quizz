package us.quizz.repository;

import us.quizz.entities.UserAnswerFeedback;

import com.google.appengine.api.datastore.Key;

public class UserAnswerFeedbackRepository extends BaseRepository<UserAnswerFeedback>{
	
	public UserAnswerFeedbackRepository() {
		super(UserAnswerFeedback.class);
	}
	
	@Override
	protected Key getKey(UserAnswerFeedback item) {
		return item.getKey();
	}

}