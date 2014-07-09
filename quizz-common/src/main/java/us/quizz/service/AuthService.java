package us.quizz.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

public class AuthService {
  private static final Logger logger = Logger.getLogger(AuthService.class.getName());
  private static final String OAUTH_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

  private OAuthService oauthService = OAuthServiceFactory.getOAuthService();
  private boolean isDev = SystemProperty.environment.value() != SystemProperty.Environment.Value.Production;

  public boolean isUserAdmin() {
    if(isDev) {
      return true;
    }

    try {
      if(oauthService.getCurrentUser(OAUTH_SCOPE) != null && oauthService.isUserAdmin(OAUTH_SCOPE)) {
        return true;
      } else {
        return false;
      }
    } catch (OAuthRequestException e) {
      logger.log(Level.INFO, "failed to get user for scope '" + OAUTH_SCOPE + "'", e);
      return false;
    }
  }
}
