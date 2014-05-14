package us.quizz.repository;

import us.quizz.entities.Experiment;
import us.quizz.ofy.OfyBaseRepository;

public class ExperimentRepository extends OfyBaseRepository<Experiment> {
  public ExperimentRepository() {
    super(Experiment.class);
  }
}
