package com.ipeirotis.crowdquiz.ads;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.utils.AuthUtils;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
import com.google.appengine.api.users.UserServiceFactory;

public class OAuth2CallbackServlet extends AbstractAppEngineAuthorizationCodeCallbackServlet {
	
	  private static final long serialVersionUID = 1L;

	  @Override
	  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
	      throws ServletException, IOException {
		  resp.sendRedirect("/campaignManagament");
	  }

	  @Override
	  protected void onError(HttpServletRequest req, HttpServletResponse resp, 
			  AuthorizationCodeResponseUrl errorResponse) throws ServletException, IOException {
		  String nickname = UserServiceFactory.getUserService().getCurrentUser().getNickname();
          resp.getWriter().print("<p>" + nickname + ", you've declined to authorize this application.</p>");
          resp.setStatus(200);
          resp.addHeader("Content-Type", "text/html");
	  }

	  @Override
	  protected String getRedirectUri(HttpServletRequest req)
			throws ServletException, IOException {
		  return AuthUtils.getRedirectUri(req);
	  }

	  @Override
	  protected AuthorizationCodeFlow initializeFlow() throws ServletException,
	  		IOException {
		  return AuthUtils.getAuthorizationCodeFlow();
	  }
}
