package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.inject.Inject;

import us.quizz.entities.User;
import us.quizz.enums.QuestionSelectionStrategy;
import us.quizz.service.UserReferralService;
import us.quizz.service.UserService;
import us.quizz.utils.Constants;
import us.quizz.utils.Security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(name = "quizz",
     description = "The API for Quizz.us",
     version = "v1",
     clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
                  Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
                  API_EXPLORER_CLIENT_ID},
     scopes = {Constants.EMAIL_SCOPE})
public class UserEndpoint {
  private UserService userService;
  private UserReferralService userReferralService;

  @Inject
  public UserEndpoint(UserService userService, UserReferralService userReferralService) {
    this.userService = userService;
    this.userReferralService = userReferralService;
  }

  /**
   * Gets the User from the request, or create the new one, if there is no cookie.
   *
   * @param req an httpRequest (to get the userID).
   * @param referer the (url) referer of the user.
   * @param quizID can be null if the user comes directly to the Quizz homepage.
   * @param userID can be null if the user is new, and then we need to create a new one for her.
   */
  @ApiMethod(name = "getUser", path = "getUser", httpMethod = HttpMethod.POST)
  public Map<String, Object> getUser(HttpServletRequest req,
      @Named("referer") String referer, @Nullable @Named("quizID") String quizID,
      @Nullable @Named("userID") String userID) {
    // If the user is new or the userid no longer exists in the datastore, try to get it from the
    // cookie and creates one if needed.
    if (userID == null || userService.get(userID) == null) {
      userID = userService.getUseridFromCookie(req);
    }
    userReferralService.asyncCreateAndStoreUserReferal(req, userID, referer, quizID);
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("userid", userID);
    return result;
  }

  /**
   * Returns a map of all users to their question selection strategy.
   * TODO(kobren): currently this code doesn't take long to run but in the future we might
   *               favor something with more api fetches to smaller subsets of the data.
   */
  @ApiMethod(name = "getUsersAndStrategies", path = "getUsersAndStrategies",
      httpMethod = HttpMethod.GET)
  public Map<String, String> getUsersAndStrategies(com.google.appengine.api.users.User user)
      throws UnauthorizedException{
    Security.verifyAuthenticatedUser(user);
    List<User> quizzUsers = userService.listAll();
    Map<String, String> userToStrategy = new HashMap<>();
    for (User quizzUser : quizzUsers) {
      // TODO(kobren): for now, the users we care about have a single question selection strategy
      //               but in future implementations this might change (and so will the following).
      userToStrategy.put(quizzUser.getUserid(), quizzUser.pickQuestionSelectionStrategy().name());
    }
    return userToStrategy;
  }
}
