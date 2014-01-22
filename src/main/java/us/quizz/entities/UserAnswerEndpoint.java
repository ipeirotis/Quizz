package us.quizz.entities;

import java.util.List;

import javax.inject.Named;

import us.quizz.enums.AnswerChallengeStatus;
import us.quizz.repository.AnswerChallengeCounterRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.utils.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1", namespace = @ApiNamespace(ownerDomain = "www.quizz.us", ownerName = "www.quizz.us", packagePath = "crowdquiz.entities"))
public class UserAnswerEndpoint extends BaseCollectionEndpoint<UserAnswer> {

	public UserAnswerEndpoint() {
		super(UserAnswer.class, "User answer");
	}

	@Override
	protected Key getKey(UserAnswer item) {
		return item.getKey();
	}

	@ApiMethod(name = "addAnswerChallenge", httpMethod=HttpMethod.POST, path="addAnswerChallenge")
	public UserAnswer addAnswerFeedBack(@Named("quizID") String quizID,
			@Named("questionID") Long questionID, 
			@Named("userAnswerID") Long userAnswerID,
			@Named("userid") String userid,
			@Named("message") String message) {
		AnswerChallengeCounter cc = AnswerChallengeCounterRepository.get(quizID, questionID);
		if(cc == null){
			cc = new AnswerChallengeCounter(quizID, questionID);
			cc.setCount(1L);
		} else {
			cc.incCount();
		}
		AnswerChallengeCounterRepository.save(cc);
		
		UserAnswer userAnswer = get(userAnswerID);
		userAnswer.setAnswerChallengeText(new Text(message));
		
		List<UserAnswer> userAnswers = UserAnswerRepository
				.getUserAnswersWithChallenge(quizID, userid);
		
		if(userAnswers.size() == 0)
			userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);
		else{
			boolean exist = false;
			for(UserAnswer ua : userAnswers){
				if(ua.getAnswerChallengeText().equals(message)){
					userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);
					
					User user = PMF.singleGetObjectById(User.class, userAnswer.getUserid());
					user.incChallengeBudget();
					PMF.singleMakePersistent(user);
					exist = true;
					break;
				}
			}
			if(!exist)
				userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);
		}
		
		
		return update(userAnswer);
	}
	
	@ApiMethod(name = "approveAnswerChallenge", path="answerChallenge/approve")
	public UserAnswer approveChallenge(@Named("userAnswerID") Long userAnswerID) {
		UserAnswer userAnswer = PMF.singleGetObjectById(UserAnswer.class, userAnswerID);
		userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);
		
		User user = PMF.singleGetObjectById(User.class, userAnswer.getUserid());
		user.incChallengeBudget();
		PMF.singleMakePersistent(user);
		
		return update(userAnswer);
	}

	@ApiMethod(name = "rejectAnswerChallenge", path="answerChallenge/reject")
	public UserAnswer rejectChallenge(@Named("userAnswerID") Long userAnswerID) {
		UserAnswer userAnswer = PMF.singleGetObjectById(UserAnswer.class, userAnswerID);
		userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);
		
		User user = PMF.singleGetObjectById(User.class, userAnswer.getUserid());
		user.decChallengeBudget();
		PMF.singleMakePersistent(user);
		
		return update(userAnswer);
	} 

}