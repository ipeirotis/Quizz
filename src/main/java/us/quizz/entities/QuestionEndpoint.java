package us.quizz.entities;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import us.quizz.repository.QuizQuestionRepository;
import us.quizz.utils.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Key;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.entities"))
public class QuestionEndpoint extends BaseCollectionEndpoint<Question> {

	public QuestionEndpoint() {
		super(Question.class, "Question");
	}

	@Override
	protected Key getKey(Question item) {
		return item.getKey();
	}

	@ApiMethod(name = "listQuestions")
	public CollectionResponse<Question> listQuestions(
			@Nullable @Named("cursor") String cursor) {
		List<Question> questions = QuizQuestionRepository.getQuizQuestions();
		return CollectionResponse.<Question> builder().setItems(questions)
				.setNextPageToken(cursor).build();
	}

	@ApiMethod(name = "getQuestion")
	public Question getQuestion(@Named("questionID") Long questionID) {
		return PMF.singleGetObjectById(Question.class, questionID);
	}

	@ApiMethod(name = "insertQuestion")
	public Question insertQuestion(Question newQuestion) {
		return insert(newQuestion);
	}

	@ApiMethod(name = "updateQuestion")
	public Question updateQuestion(Question newQuestion) {
		return update(newQuestion);
	}

	@ApiMethod(name = "removeQuestion")
	public void removeQuestion(@Named("questionID") Long questionID) {
		remove(getKey(getQuestion(questionID)));
	}

}