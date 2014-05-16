package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Treatment;
import us.quizz.repository.TreatmentRepository;

import java.util.List;

public class TreatmentService {
  private TreatmentRepository treatmentRepository;

  @Inject
  public TreatmentService(TreatmentRepository treatmentRepository){
    this.treatmentRepository = treatmentRepository;
  }

  public Treatment save(Treatment treatment){
    return treatmentRepository.saveAndGet(treatment);
  }

  public List<Treatment> list(){
    return treatmentRepository.list();
  }
}
