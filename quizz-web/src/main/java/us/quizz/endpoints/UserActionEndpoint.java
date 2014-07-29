package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.inject.Inject;

import us.quizz.service.UserActionService;
import us.quizz.utils.Constants;

import java.util.Date;

import javax.inject.Named;

@Api(name = "quizz",
     description = "The API for Quizz.us",
     version = "v1",
     clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
                  Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
                  API_EXPLORER_CLIENT_ID},
     scopes = {Constants.EMAIL_SCOPE})
public class UserActionEndpoint {
  private UserActionService userActionService;

  @Inject
  public UserActionEndpoint(UserActionService userActionService) {
    this.userActionService = userActionService;
  }

  @ApiMethod(name = "recordQuestionShown", path = "recordQuestionShown",
             httpMethod = HttpMethod.POST)
  public void recordQuestionShown(
      @Named("userID") String userID,
      @Named("quizID") String quizID,
      @Named("questionID") Long questionID) {
    Long timestamp = (new Date()).getTime();
    userActionService.asyncSaveQuestionShown(userID, timestamp, quizID, questionID);
  }

  @ApiMethod(name = "recordExpandQuestionContext", path = "recordExpandQuestionContext",
             httpMethod = HttpMethod.POST)
  public void recordExpandQuestionContext(
      @Named("userID") String userID,
      @Named("quizID") String quizID,
      @Named("questionID") Long questionID) {
    Long timestamp = (new Date()).getTime();
    userActionService.asyncSaveExpandQuestionContext(userID, timestamp, quizID, questionID);
  }

  @ApiMethod(name = "recordHideQuestionContext", path = "recordHideQuestionContext",
             httpMethod = HttpMethod.POST)
  public void recordHideQuestionContext(
      @Named("userID") String userID,
      @Named("quizID") String quizID,
      @Named("questionID") Long questionID) {
    Long timestamp = (new Date()).getTime();
    userActionService.asyncSaveHideQuestionContext(userID, timestamp, quizID, questionID);
  }
}
