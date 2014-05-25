package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Treatment;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.TreatmentRepository;

public class TreatmentService extends OfyBaseService<Treatment> {
  @Inject
  public TreatmentService(TreatmentRepository treatmentRepository){
    super(treatmentRepository);
  }
}
