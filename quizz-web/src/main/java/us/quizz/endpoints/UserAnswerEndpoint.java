package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.enums.AnswerChallengeStatus;
import us.quizz.service.AnswerChallengeCounterService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserService;

import java.util.List;

import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class UserAnswerEndpoint {
  private UserAnswerService userAnswerService;
  private UserService userService;
  private AnswerChallengeCounterService answerChallengeCounterService;

  @Inject
  public UserAnswerEndpoint(
      UserAnswerService userAnswerService,
      UserService userService,
      AnswerChallengeCounterService answerChallengeCounterService) {
    this.userAnswerService = userAnswerService;
    this.userService = userService;
    this.answerChallengeCounterService = answerChallengeCounterService;
  }

  @ApiMethod(name = "addAnswerFeedback", httpMethod = HttpMethod.POST, path = "addAnswerFeedback")
  public UserAnswer addAnswerFeedback(
      @Named("quizID") String quizID,
      @Named("questionID") Long questionID, 
      @Named("userAnswerID") Long userAnswerID,
      @Named("userid") String userid,
      @Named("message") String message) {
    AnswerChallengeCounter cc = answerChallengeCounterService.get(quizID, questionID);
    if (cc == null) {
      cc = new AnswerChallengeCounter(quizID, questionID);
      cc.setCount(1L);
    } else {
      cc.incCount();
    }
    answerChallengeCounterService.save(cc);

    UserAnswer userAnswer = userAnswerService.get(userAnswerID);
    userAnswer.setAnswerChallengeText(new Text(message));

    List<UserAnswer> userAnswers = userAnswerService.getUserAnswers(quizID, userid);
    if (userAnswers.size() != 0) {
      boolean exist = false;
      for (UserAnswer ua : userAnswers) {
        if (ua.getAnswerChallengeText() != null && ua.getAnswerChallengeText().equals(message)) {
          userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.APPROVED);

          User user = userService.get(userAnswer.getUserid());
          user.incChallengeBudget();
          userService.save(user);
          exist = true;
          break;
        }
      }
      if (!exist) {
        userAnswer.setAnswerChallengeStatus(AnswerChallengeStatus.REJECTED);
      }
    }
    return userAnswerService.save(userAnswer);
  }
}
