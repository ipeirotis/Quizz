package us.quizz.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.common.collect.Lists;

public class AuthUtils {
	
	public static final String CLIENT_ID = System.getProperty("adwords.appengine.clientId");
	public static final String CLIENT_SECRET = System.getProperty("adwords.appengine.clientSecret");
	public static final String CALLBACK_URI = "/oauth2callback";
	public static final String SCOPE = "https://adwords.google.com/api/adwords/";

	private static final DataStoreFactory STORE_FACTORY = new MemoryDataStoreFactory();

	public static String getRedirectUri(HttpServletRequest req)
			throws ServletException, IOException {
	    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
	    url.setRawPath(CALLBACK_URI);
	    return url.build(); 
	}

	public static AuthorizationCodeFlow getAuthorizationCodeFlow() throws ServletException,
			IOException {

		return new GoogleAuthorizationCodeFlow.Builder(
			     new NetHttpTransport(),
			     new JacksonFactory(),
			     CLIENT_ID,
			     CLIENT_SECRET,
			     Lists.newArrayList(SCOPE))
			     .setDataStoreFactory(STORE_FACTORY)
			     .setApprovalPrompt("force")
			     .setAccessType("offline").build();
	}
}
