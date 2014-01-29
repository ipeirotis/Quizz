package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.User;
import us.quizz.repository.UserRepository;
import us.quizz.utils.ServletUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class FacebookLogin extends HttpServlet {
	
	private UserRepository userRepository;
	
	@Inject
	public FacebookLogin(UserRepository userRepository){
		this.userRepository = userRepository;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		User user = null;
		ServletUtils.ensureParameters(req, "fbid", "sessionid", "url");
		String fbid = req.getParameter("fbid");
		String sessionid = req.getParameter("sessionid");
		user = userRepository.getUseridFromSocialid(fbid);
		if (user == null) {
			user = userRepository.getUseridFromCookie(req, resp);
			user.setSocialid(fbid);
		}
		user.setSessionid(sessionid);
		userRepository.singleMakePersistent(user);
	}
};
