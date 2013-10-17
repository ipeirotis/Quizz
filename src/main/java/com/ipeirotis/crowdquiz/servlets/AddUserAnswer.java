package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddUserAnswer extends HttpServlet {

	final static Logger	logger	= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("text/plain");

		String action = req.getParameter("action").replace('\t', ' ');
		String userid = req.getParameter("userid").replace('\t', ' ');
		String questionStrID = req.getParameter("questionID").replace('\t', ' ');
		Long questionID = Long.parseLong(questionStrID);
		String useranswer = req.getParameter("useranswer");
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
		
		Long answerId = Long.parseLong(useranswer);
		UserAnswer ue = new UserAnswer(userid, questionID, answerId);
		ue.setReferer(referer);
		ue.setBrowser(browser);
		ue.setIpaddress(ipAddress);
		ue.setTimestamp(timestamp);
		ue.setAction(action);
		if (isCorrect!=null) ue.setIsCorrect(isCorrect);

		PMF.singleMakePersistent(ue);

		resp.getWriter().println("OK");
	}

}
