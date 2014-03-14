package us.quizz.endpoints;

import javax.inject.Named;

import us.quizz.repository.QuizRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "crowd-power.appspot.com", ownerName = "crowd-power.appspot.com", packagePath = "crowdquiz.endpoints"))
public class UpdateQuizCountsEndpoint {
	
	private QuizRepository quizRepository;
	
	@Inject
	public UpdateQuizCountsEndpoint(QuizRepository quizRepository){
		this.quizRepository = quizRepository;
	}

	@ApiMethod(name = "updateQuizCounts", path="updateQuizCounts")
	public void getQuizCounts(@Named("quizID") String quizID) {
		quizRepository.updateQuizCounts(quizID);
	} 

}
