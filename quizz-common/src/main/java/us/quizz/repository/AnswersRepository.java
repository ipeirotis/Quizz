package us.quizz.repository;

import us.quizz.entities.Answer;

import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

public class AnswersRepository extends BaseRepository<Answer>{
	
	@Inject
	QuizQuestionRepository quizQuestionRepository;
	
	public AnswersRepository() {
		super(Answer.class);
	}
	
	@Override
	protected Key getKey(Answer item) {
		return item.getID();
	}

	public Answer getAnswer(Long questionID, Integer answerID) {
		return quizQuestionRepository.getQuizQuestion(questionID).getAnswer(
				answerID);
	}

}
