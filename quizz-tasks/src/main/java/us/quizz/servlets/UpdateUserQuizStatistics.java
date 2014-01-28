package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.QuizPerformance;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.utils.ChannelHelpers;
import us.quizz.utils.ServletUtils;

import com.google.gson.Gson;

/**
 * 
 * Takes as input a userid and a quiz, updates the user scores for the quiz, and
 * then computes the rank of the user within the set of all other users.
 * Finally, it puts the QuizPerformance object in the memcache for quick
 * retrieval.
 * 
 * @author ipeirotis
 * 
 */
@SuppressWarnings("serial")
public class UpdateUserQuizStatistics extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");

		ServletUtils.ensureParameters(req, "quizID", "userid");
		String quiz = req.getParameter("quizID");
		String userid = req.getParameter("userid");

		// QuizPerformance qp =
		// QuizPerformanceRepository.getQuizPerformance(quiz, userid);
		// if (qp==null) {
		QuizPerformance qp = new QuizPerformance(quiz, userid);
		// }
		qp.computeCorrect();

		/*
		 * if (qp.getTotalanswers()==0) {
		 * QuizPerformanceRepository.deleteQuizPerformance(quiz, userid);
		 * return; }
		 */

		qp.computeRank();
		QuizPerformanceRepository.storeQuizPerformance(qp);
		String channelNotify = req.getParameter("channelNotify");
		if (Boolean.parseBoolean(channelNotify)) {
			notifyUserViaChannel(userid, quiz);
		}
	}

	protected void notifyUserViaChannel(String userId, String quizID) {
		ChannelHelpers ch = new ChannelHelpers();
		String channelId = ch.generateUserQuizChannelID(userId, quizID);
		String jQuizPerformance = new Gson().toJson(QuizPerformanceRepository
				.getQuizPerformance(quizID, userId));
		ch.sendMessage(channelId, jQuizPerformance);
	}
}
