package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Experiment;
import us.quizz.entities.Treatment;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.ExperimentRepository;
import us.quizz.repository.TreatmentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO(chunhowt): Write unit tests for this class by injecting the random generator.
public class ExperimentService extends OfyBaseService<Experiment> {
  private TreatmentRepository treatmentRepository;

  @Inject
  public ExperimentService(ExperimentRepository experimentRepository, 
      TreatmentRepository treatmentRepository){
    super(experimentRepository);
    this.treatmentRepository = treatmentRepository;
  }

  public Experiment save(Experiment experiment) {
    if (experiment.getTreatments() == null){
      assignTreatments(experiment);
    }
    return baseRepository.saveAndGet(experiment);
  }

  // Going over all the active treatments in the datastore and assign
  // treatments according to their probabilities.
  private void assignTreatments(Experiment experiment) {
    List<Treatment> allTreatments = treatmentRepository.listAllByCursor();

    Map<String, Boolean> treatments = new HashMap<String, Boolean>();
    for (Treatment t : allTreatments) {
      Boolean activate = (Math.random() < t.getProbability());
      treatments.put(t.getName(), activate);
    }
    experiment.setTreatments(treatments);
  }
}
