package us.quizz.repository;

import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.ofy.OfyBaseRepository;

public class SurvivalProbabilityResultRepository extends OfyBaseRepository<SurvivalProbabilityResult> {
  public SurvivalProbabilityResultRepository() {
    super(SurvivalProbabilityResult.class);
  }
}
