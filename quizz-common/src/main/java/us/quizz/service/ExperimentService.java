package us.quizz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.quizz.entities.Experiment;
import us.quizz.entities.Treatment;
import us.quizz.repository.ExperimentRepository;

import com.google.inject.Inject;

public class ExperimentService {

  private ExperimentRepository experimentRepository;
  private TreatmentService treatmentService;
  
  @Inject
  public ExperimentService(ExperimentRepository experimentRepository, 
      TreatmentService treatmentService){
    this.experimentRepository = experimentRepository;
    this.treatmentService = treatmentService;
  }

  public Experiment get(Long id) {
    return experimentRepository.get(id);
  }
  
  public Experiment save(Experiment experiment) {
    if(experiment.getTreatments() == null){
      assignTreatments(experiment);
    }
    return experimentRepository.saveAndGet(experiment);
  }
  
  public void assignTreatments(Experiment experiment) {
    // Going over all the active treatments in the datastore and assign
    // treatments according to their probabilities.

    // At this point, we do not use/support the blocking functionality
    List<Treatment> allTreatments = treatmentService.list();

    Map<String, Boolean> treatments = new HashMap<String, Boolean>();
    for (Treatment t : allTreatments) {
      Boolean activate = (Math.random() < t.getProbability());
      treatments.put(t.getName(), activate);
    }
    experiment.setTreatments(treatments);
  }
}
