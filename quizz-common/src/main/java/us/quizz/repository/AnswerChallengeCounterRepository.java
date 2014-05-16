package us.quizz.repository;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.ofy.OfyBaseRepository;

public class AnswerChallengeCounterRepository extends OfyBaseRepository<AnswerChallengeCounter> {
  public AnswerChallengeCounterRepository() {
    super(AnswerChallengeCounter.class);
  }
}
