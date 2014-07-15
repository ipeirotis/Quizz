package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.inject.Inject;

import us.quizz.service.UserReferralService;
import us.quizz.service.UserService;
import us.quizz.utils.Constants;

import java.util.HashMap;
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

  // Gets the User from the request, or create the new one, if there is no cookie.
  // quizID can be null if the user comes directly to the Quizz homepage.
  @ApiMethod(name = "getUser", path = "getUser", httpMethod = HttpMethod.POST)
  public Map<String, Object> getUser(HttpServletRequest req,
      @Named("referer") String referer, @Nullable @Named("quizID") String quizID) {
    String userid = userService.getUseridFromCookie(req);
    userReferralService.asyncCreateAndStoreUserReferal(req, userid, referer, quizID);

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("userid", userid);
    return result;
  }
}
