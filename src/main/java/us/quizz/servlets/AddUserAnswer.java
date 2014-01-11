package us.quizz.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.utils.PMF;

@SuppressWarnings("serial")
public class AddUserAnswer extends HttpServlet {

	final static Logger logger = Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/plain");

		Utils.ensureParameters(req, "action", "userid", "questionID",
				"useranswer");

		String action = req.getParameter("action").replace('\t', ' ');
		String userid = req.getParameter("userid").replace('\t', ' ');
		String questionStrID = req.getParameter("questionID");
		Long questionID = Long.parseLong(questionStrID);
		Integer useranswerID = Integer.parseInt(req.getParameter("useranswer"));
		String correct = req.getParameter("correct");
		Boolean isCorrect = (correct.equals("true"));
		String browser = req.getParameter("browser");
		String ipAddress = req.getParameter("ipAddress");
		String referer = req.getParameter("referer");
		String time = req.getParameter("timestamp");
		Long timestamp = -1L;
		if (time != null) {
			timestamp = Long.parseLong(time);
		} else {
			return;
		}

		UserAnswer ue = new UserAnswer(userid, questionID, useranswerID);
		ue.setReferer(referer);
		ue.setBrowser(browser);
		ue.setIpaddress(ipAddress);
		ue.setTimestamp(timestamp);
		ue.setAction(action);
		ue.setQuizID(QuizQuestionRepository.getQuizQuestion(questionID)
				.getQuizID());
		if (isCorrect != null)
			ue.setIsCorrect(isCorrect);

		PMF.singleMakePersistent(ue);

		resp.getWriter().println("OK");
	}

}
