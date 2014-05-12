package us.quizz.service;

import us.quizz.entities.Treatment;
import us.quizz.repository.TreatmentRepository;

import com.google.inject.Inject;

public class TreatmentService {

  private TreatmentRepository treatmentRepository;
  
  @Inject
  public TreatmentService(TreatmentRepository treatmentRepository){
    this.treatmentRepository = treatmentRepository;
  }
  
  public Treatment save(Treatment treatment){
    return treatmentRepository.saveAndGet(treatment);
  }
}
