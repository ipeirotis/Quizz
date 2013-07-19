package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
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

		String action = req.getParameter("action");
		String userid = req.getParameter("userid");
		String relation = req.getParameter("relation");
		String mid = req.getParameter("mid");
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

		UserAnswer ue = new UserAnswer(userid, relation, mid, useranswer);
		ue.setReferer(referer);
		ue.setBrowser(browser);
		ue.setIpaddress(ipAddress);
		ue.setTimestamp(timestamp);
		ue.setAction(action);
		if (isCorrect!=null) ue.setIsCorrect(isCorrect);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ue);
		pm.close();

		resp.getWriter().println("OK");

	}

}
