package us.quizz.endpoints;

import java.util.ArrayList;

import us.quizz.entities.Question;
import us.quizz.repository.QuizQuestionRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;
import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class ReportsEndpoint {
	
	private QuizQuestionRepository quizQuestionRepository;
	
	@Inject
	public ReportsEndpoint(QuizQuestionRepository quizQuestionRepository){
		this.quizQuestionRepository = quizQuestionRepository;
	}

	@ApiMethod(name = "report", path="report")
	public ArrayList<Question> get(@Named("quizID")String quizID) {
		return quizQuestionRepository.getQuizQuestions(quizID);
	} 

}