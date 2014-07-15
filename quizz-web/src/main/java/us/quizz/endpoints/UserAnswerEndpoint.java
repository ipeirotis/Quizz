package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.inject.Inject;

import us.quizz.entities.AnswerChallengeCounter;
import us.quizz.entities.UserAnswer;
import us.quizz.service.AnswerChallengeCounterService;
import us.quizz.service.UserAnswerService;
import us.quizz.service.UserService;
import us.quizz.utils.Constants;
import us.quizz.utils.Security;

import java.util.List;

import javax.inject.Named;

@Api(name = "quizz",
     description = "The API for Quizz.us",
     version = "v1",
     clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
                  Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
                  API_EXPLORER_CLIENT_ID},
     scopes = {Constants.EMAIL_SCOPE})
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

  @ApiMethod(name = "getUserAnswers", httpMethod = HttpMethod.POST,
             path = "getUserAnswers")
  public List<UserAnswer> getUserAnswers(@Named("quizID") String quizID, User user)
      throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    return userAnswerService.getUserAnswersForQuiz(quizID);
  }

  // Adds user's answer challenge text to the corresponding UserAnswer identified by the
  // userAnswerID.
  @ApiMethod(name = "addAnswerFeedback", httpMethod = HttpMethod.POST, path = "addAnswerFeedback")
  public UserAnswer addAnswerFeedback(
      @Named("quizID") String quizID,
      @Named("questionID") Long questionID, 
      @Named("userAnswerID") Long userAnswerID,
      @Named("userid") String userid,
      @Named("message") String message,
      @Named("correctValue") String correctValue,
      @Named("urlSupport") String urlSupport,
      @Named("challengeReason") String challengeReason) {
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
    userAnswer.setAnswerChallengeCorrectValue(new Text(correctValue));
    userAnswer.setAnswerChallengeUrlSupport(new Text(urlSupport));
    userAnswer.setAnswerChallengeReason(challengeReason);

    // TODO(chunhowt): Check whether user answer text is valid by comparing against other user
    // input.
    return userAnswerService.save(userAnswer);
  }
}
