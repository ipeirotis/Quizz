package us.quizz.servlets;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.entities.User;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.utils.ChannelHelpers;
import us.quizz.utils.Helper;

@SuppressWarnings("serial")
@Singleton
public class StartQuiz extends HttpServlet {
	
	private UserRepository userRepository;
	private UserReferralRepository userReferralRepository;
	
	@Inject
	public StartQuiz(UserRepository userRepository, UserReferralRepository userReferralRepository){
		this.userRepository = userRepository;
		this.userReferralRepository = userReferralRepository;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String quizID = req.getParameter("quizID");

		String gclid = req.getParameter("gclid");

		User user = userRepository.getUseridFromCookie(req, resp);
		String userid = user.getUserid();
		userReferralRepository.createAndStoreUserReferal(req, userid);

		String nextURL = Helper.getBaseURL(req)
				+ "/multiChoiceMulti.jsp?quizID="
				+ URLEncoder.encode(quizID, "UTF-8");
		if (gclid != null) {
			nextURL += "&gclid=" + gclid;
		}

		String userChannelId = ChannelHelpers.generateUserQuizChannelID(user,
				quizID);
		String token = ChannelHelpers.createChannel(userChannelId);

		nextURL += "&changelToken=" + URLEncoder.encode(token, "UTF-8");
		resp.sendRedirect(nextURL);
	}

}
