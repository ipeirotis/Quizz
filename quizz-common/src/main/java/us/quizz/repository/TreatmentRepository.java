package us.quizz.repository;

import us.quizz.entities.Treatment;
import us.quizz.ofy.OfyBaseRepository;

public class TreatmentRepository extends OfyBaseRepository<Treatment> {
  public TreatmentRepository() {
    super(Treatment.class);
  }
}
