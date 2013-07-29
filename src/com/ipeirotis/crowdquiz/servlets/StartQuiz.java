package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.entities.UserReferal;
import com.ipeirotis.crowdquiz.utils.Helper;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class StartQuiz extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String relation = req.getParameter("relation");
		User user = User.getUseridFromCookie(req, resp);
		String userid = user.getUserid();
		storeUserReferal(req, userid);

		String nextURL = Helper.getNextMultipleChoiceURL(req, relation, userid, null);

		resp.sendRedirect(nextURL);
	}

	/**
	 * @param req
	 * @param userid
	 */
	private void storeUserReferal(HttpServletRequest req, String userid) {

		UserReferal ur = new UserReferal(userid);
		ur.setQuiz(req.getParameter("relation"));
		ur.setReferer(req.getHeader("Referer"));
		ur.setIpaddress(req.getRemoteAddr());
		ur.setBrowser(req.getHeader("User-Agent"));
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(ur);
		pm.close();
	}

}
