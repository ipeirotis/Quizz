package us.quizz.endpoints;

import java.util.List;

import javax.inject.Named;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerChallengeStatus;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserRepository;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.endpoints"))
public class UserAnswerEndpoint {
	
	private UserAnswerRepository userAnswerRepository;
	private UserRepository userRepository;
	private AnswerChallengeCounterRepository answerChallengeCounterRepository;
	
	@Inject
	public UserAnswerEndpoint(UserAnswerRepository userAnswerRepository, UserRepository userRepository,
			AnswerChallengeCounterRepository answerChallengeCounterRepository){
		this.userAnswerRepository = userAnswerRepository;
		this.userRepository = userRepository;
		this.answerChallengeCounterRepository = answerChallengeCounterRepository;
	}

	@ApiMethod(name = "addAnswerChallenge", httpMethod=HttpMethod.POST, path="addAnswerChallenge")
	public UserAnswer addAnswerFeedBack(@Named("quizID") String quizID,
			@Named("questionID") Long questionID, 
			@Named("userAnswerID") Long userAnswerID,
			@Named("userid") String userid,
			@Named("message") String message) {
		AnswerChallengeCounter cc = answerChallengeCounterRepository.get(quizID, questionID);
		if(cc == null){
			cc = new AnswerChallengeCounter(quizID, questionID);
			cc.setCount(1L);
		} else {
			cc.incCount();
		}
		answerChallengeCounterRepository.save(cc);
		
		UserAnswer userAnswer = userAnswerRepository.get(userAnswerID);
		userAnswer.setAnswerChallengeText(new Text(message));
		
		List<UserAnswer> userAnswers = userAnswerRepository
				.getUserAnswersWithChallenge(quizID, userid);
		
		if(userAnswers.size() != 0){
			boolean exist = false;
			for(UserAnswer ua : userAnswers){
				if(ua.getAnswerChallengeText().equals(message)){
					userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);
					
					User user = userRepository.singleGetObjectById(User.class, userAnswer.getUserid());
					user.incChallengeBudget();
					userRepository.singleMakePersistent(user);
					exist = true;
					break;
				}
			}
			if(!exist)
				userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);
		}
		
		return userAnswerRepository.update(userAnswer);
	}
	
	@ApiMethod(name = "approveAnswerChallenge", path="answerChallenge/approve")
	public UserAnswer approveChallenge(@Named("userAnswerID") Long userAnswerID) {
		UserAnswer userAnswer = userAnswerRepository.singleGetObjectById(UserAnswer.class, userAnswerID);
		userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);
		
		User user = userRepository.singleGetObjectById(User.class, userAnswer.getUserid());
		user.incChallengeBudget();
		userRepository.singleMakePersistent(user);
		
		return userAnswerRepository.update(userAnswer);
	}

	@ApiMethod(name = "rejectAnswerChallenge", path="answerChallenge/reject")
	public UserAnswer rejectChallenge(@Named("userAnswerID") Long userAnswerID) {
		UserAnswer userAnswer = userAnswerRepository.singleGetObjectById(UserAnswer.class, userAnswerID);
		userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);
		
		User user = userRepository.singleGetObjectById(User.class, userAnswer.getUserid());
		user.decChallengeBudget();
		userRepository.singleMakePersistent(user);
		
		return userAnswerRepository.update(userAnswer);
	} 

}