package us.quizz.repository;

import us.quizz.entities.Question;
import us.quizz.ofy.OfyBaseRepository;

public class QuestionRepository extends OfyBaseRepository<Question> {
  public QuestionRepository() {
    super(Question.class);
  }
}
