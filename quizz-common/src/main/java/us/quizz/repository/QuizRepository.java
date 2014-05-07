package us.quizz.repository;

import us.quizz.entities.Quiz;
import us.quizz.ofy.OfyBaseRepository;

public class QuizRepository  extends OfyBaseRepository<Quiz>{
  
  public QuizRepository() {
    super(Quiz.class);
  }

}
