package us.quizz.endpoints;

import javax.inject.Named;

import us.quizz.entities.Treatment;
import us.quizz.repository.TreatmentRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class TreatmentEndpoint {
	
	private TreatmentRepository treatmentRepository;
	
	@Inject
	public TreatmentEndpoint(TreatmentRepository treatmentRepository){
		this.treatmentRepository = treatmentRepository;
	}

	@ApiMethod(name = "addTreatment", path="addTreatment", httpMethod=HttpMethod.POST)
	public void addTreatment(@Named("name") String name, 
							@Named("probability") Double probability) {
		Treatment treatment = new Treatment(name, probability);
		treatmentRepository.singleMakePersistent(treatment);
	}

}