package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.inject.Inject;

import us.quizz.service.QuizService;
import us.quizz.utils.Constants;
import us.quizz.utils.Security;

import javax.inject.Named;

@Api(name = "quizz",
     description = "The API for Quizz.us",
     version = "v1",
     clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
                  Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
                  API_EXPLORER_CLIENT_ID},
     scopes = {Constants.EMAIL_SCOPE})
public class UpdateQuizCountsEndpoint {
  private QuizService quizService;

  @Inject
  public UpdateQuizCountsEndpoint(QuizService quizService) {
    this.quizService = quizService;
  }

  @ApiMethod(name = "updateQuizCounts", path="updateQuizCounts", httpMethod = HttpMethod.GET)
  public void updateQuizCounts(@Named("quizID") String quizID, User user)
      throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    quizService.updateQuizCounts(quizID);
  }
}
