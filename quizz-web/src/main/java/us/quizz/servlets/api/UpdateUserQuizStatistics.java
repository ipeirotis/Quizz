package us.quizz.servlets.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.utils.ChannelHelpers;
import us.quizz.utils.ServletUtils;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
@Singleton
public class UpdateUserQuizStatistics extends HttpServlet {
	
	private UserAnswerRepository userAnswerRepository;
	private QuizPerformanceRepository quizPerformanceRepository;
	
	@Inject
	public UpdateUserQuizStatistics(UserAnswerRepository userAnswerRepository, 
			QuizPerformanceRepository quizPerformanceRepository){
		this.userAnswerRepository = userAnswerRepository;
		this.quizPerformanceRepository = quizPerformanceRepository;
	}

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
		
		List<UserAnswer> userAnswerList = userAnswerRepository.getUserAnswers(quiz, userid);
		qp.computeCorrect(userAnswerList);

		/*
		 * if (qp.getTotalanswers()==0) {
		 * QuizPerformanceRepository.deleteQuizPerformance(quiz, userid);
		 * return; }
		 */
		List<QuizPerformance> quizPerformanceList = quizPerformanceRepository
				.getQuizPerformancesByQuiz(quiz);
		qp.computeRank(quizPerformanceList);
		quizPerformanceRepository.storeQuizPerformance(qp);
		String channelNotify = req.getParameter("channelNotify");
		if (Boolean.parseBoolean(channelNotify)) {
			notifyUserViaChannel(userid, quiz, qp);
		}
	}

	protected void notifyUserViaChannel(String userId, String quizID, QuizPerformance qp) {
		ChannelHelpers.sendMessage(userId, new Gson().toJson(qp));
	}
}
