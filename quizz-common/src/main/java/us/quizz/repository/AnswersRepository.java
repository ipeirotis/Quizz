package us.quizz.repository;

import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.ofy.OfyBaseRepository;

public class AnswersRepository extends OfyBaseRepository<Answer> {
  @Inject
  public AnswersRepository() {
    super(Answer.class);
  }
}
