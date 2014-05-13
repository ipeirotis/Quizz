package us.quizz.repository;

import us.quizz.entities.QuizPerformance;
import us.quizz.ofy.OfyBaseRepository;

public class QuizPerformanceRepository extends OfyBaseRepository<QuizPerformance> {
  public QuizPerformanceRepository() {
    super(QuizPerformance.class);
  }

  public QuizPerformance get(String quizid, String userid) {
    return get(QuizPerformance.generateId(quizid, userid));
  }

  public void delete(String quizid, String userid) {
    delete(QuizPerformance.generateId(quizid, userid));
  }

}
