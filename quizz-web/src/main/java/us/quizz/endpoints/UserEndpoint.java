package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.inject.Inject;

import us.quizz.service.UserReferralService;
import us.quizz.service.UserService;
import us.quizz.utils.ChannelHelpers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
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
    result.put("token", ChannelHelpers.createChannel(userid));
    return result;
  }
}
