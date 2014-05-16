package us.quizz.repository;

import us.quizz.entities.Answer;
import us.quizz.ofy.OfyBaseRepository;

import com.google.inject.Inject;

public class AnswersRepository extends OfyBaseRepository<Answer> {
  @Inject
  public AnswersRepository() {
    super(Answer.class);
  }
}
