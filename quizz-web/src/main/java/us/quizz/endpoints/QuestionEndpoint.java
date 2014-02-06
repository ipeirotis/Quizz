package us.quizz.endpoints;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.Question;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.QuizQuestionRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class QuestionEndpoint {
	
	private QuizQuestionRepository quizQuestionRepository;
	private AnswerChallengeCounterRepository answerChallengeCounterRepository;
	
	@Inject
	public QuestionEndpoint(QuizQuestionRepository quizQuestionRepository,
			AnswerChallengeCounterRepository answerChallengeCounterRepository){
		this.quizQuestionRepository = quizQuestionRepository;
		this.answerChallengeCounterRepository = answerChallengeCounterRepository;
	}

	@ApiMethod(name = "listQuestions")
	public CollectionResponse<Question> listQuestions(
			@Nullable @Named("cursor") String cursor) {
		List<Question> questions = quizQuestionRepository.getQuizQuestions();
		return CollectionResponse.<Question> builder().setItems(questions)
				.setNextPageToken(cursor).build();
	}
	
	@ApiMethod(name = "listQuestionsWithChallenges", path="/listQuestionsWithChallenges")
	public CollectionResponse<QuestionWithChallenges> listQuestionsWithChallenges(
			@Nullable @Named("cursor") String cursor,
			@Nullable @Named("limit") Integer limit) {
		List<AnswerChallengeCounter> challenges = answerChallengeCounterRepository.list(cursor, limit);

		List<Key> keys = new ArrayList<Key>();
		for(AnswerChallengeCounter c : challenges){
			keys.add(KeyFactory.createKey(Question.class.getSimpleName(), c.getQuestionID()));
		}
		
		List<QuestionWithChallenges> result = new ArrayList<QuestionWithChallenges>();
		if(keys.size() != 0){
			List<Question> questions = quizQuestionRepository.getQuizQuestionsByKeys(keys);
			int i = 0;
			for(Question question : questions){
				result.add(new QuestionWithChallenges(question, challenges.get(i).getCount()));
				i++;
			}
		}
		
		return CollectionResponse.<QuestionWithChallenges> builder().setItems(result)
				.setNextPageToken(cursor).build();
	}

	@ApiMethod(name = "getQuestion")
	public Question getQuestion(@Named("questionID") Long questionID) {
		return quizQuestionRepository.singleGetObjectById(Question.class, questionID);
	}

	@ApiMethod(name = "insertQuestion")
	public Question insertQuestion(Question newQuestion) {
		return quizQuestionRepository.insert(newQuestion);
	}

	@ApiMethod(name = "updateQuestion")
	public Question updateQuestion(Question newQuestion) {
		return quizQuestionRepository.update(newQuestion);
	}

	@ApiMethod(name = "removeQuestion")
	public void removeQuestion(@Named("questionID") Long questionID) {
		quizQuestionRepository.remove(Question.generateKeyFromID(questionID));
	}
	
	class QuestionWithChallenges {
		private Question question;
		private Long challenges;
		
		public QuestionWithChallenges(Question question, Long challenges) {
			this.question = question;
			this.challenges = challenges;
		}

		public Question getQuestion() {
			return question;
		}

		public void setQuestion(Question question) {
			this.question = question;
		}

		public Long getChallenges() {
			return challenges;
		}

		public void setChallenges(Long challenges) {
			this.challenges = challenges;
		}
	}

}