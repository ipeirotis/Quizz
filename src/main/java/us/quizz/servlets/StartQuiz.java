package us.quizz.servlets;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.User;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserRepository;
import us.quizz.utils.Helper;

@SuppressWarnings("serial")
public class StartQuiz extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String quizID = req.getParameter("quizID");
		
		String gclid = req.getParameter("gclid");
		
		User user = UserRepository.getUseridFromCookie(req, resp);
		String userid = user.getUserid();
		UserReferralRepository.createAndStoreUserReferal(req, userid);

		String nextURL = Helper.getBaseURL(req) + "/multiChoiceMulti.jsp?quizID=" + URLEncoder.encode(quizID, "UTF-8") ;
		if (gclid != null) {
			nextURL += "&gclid="+gclid;
		}
		
		ChannelHelpers channelHelpers = new ChannelHelpers();
		String userChannelId = channelHelpers.generateUserQuizChannelID(user, quizID);
		String token = channelHelpers.createChannel(userChannelId);
		
		nextURL += "&changelToken=" + URLEncoder.encode(token, "UTF-8");
		resp.sendRedirect(nextURL);
	}

}