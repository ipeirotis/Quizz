package us.quizz.repository;

import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

import us.quizz.entities.Answer;

public class AnswersRepository extends BaseRepository<Answer> {
  QuizQuestionRepository quizQuestionRepository;

  @Inject
  public AnswersRepository(QuizQuestionRepository quizQuestionRepository) {
    super(Answer.class);
    this.quizQuestionRepository = quizQuestionRepository;
  }

  @Override
  protected Key getKey(Answer item) {
    return item.getID();
  }

  public Answer getAnswer(Long questionID, Integer answerID) {
    return quizQuestionRepository.getQuizQuestion(questionID).getAnswer(answerID);
  }
}
