package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.inject.Inject;

import us.quizz.entities.Treatment;
import us.quizz.repository.TreatmentRepository;

import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class TreatmentEndpoint {
  private TreatmentRepository treatmentRepository;

  @Inject
  public TreatmentEndpoint(TreatmentRepository treatmentRepository) {
    this.treatmentRepository = treatmentRepository;
  }

  @ApiMethod(name = "addTreatment", path = "addTreatment", httpMethod = HttpMethod.POST)
  public Treatment addTreatment(@Named("name") String name,
                                @Named("probability") Double probability) {
    return treatmentRepository.insert(new Treatment(name, probability));
  }
}
