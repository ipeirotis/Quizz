package us.quizz.endpoints;

import javax.inject.Named;

import us.quizz.entities.Treatment;
import us.quizz.repository.TreatmentRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "crowd-power.appspot.com", ownerName = "crowd-power.appspot.com", packagePath = "crowdquiz.endpoints"))
public class TreatmentEndpoint {
	
	private TreatmentRepository treatmentRepository;
	
	@Inject
	public TreatmentEndpoint(TreatmentRepository treatmentRepository){
		this.treatmentRepository = treatmentRepository;
	}

	@ApiMethod(name = "addTreatment", path="addTreatment", httpMethod=HttpMethod.POST)
	public Treatment addTreatment(@Named("name") String name, 
							@Named("probability") Double probability) {
		return treatmentRepository.insert(new Treatment(name, probability));
	}

}
