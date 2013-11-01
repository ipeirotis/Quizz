package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.User;

@SuppressWarnings("serial")
public class FacebookLogin extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = null;
		Utils.ensureParameters(req, "fbid", "sessionid", "url");
		String fbid = req.getParameter("fbid");
		String sessionid = req.getParameter("sessionid");
		user = User.getUseridFromSocialid(fbid);
		if (user == null) {
			user = User.getUseridFromCookie(req, resp);
			user.setFBID(fbid);
		}
		user.setSessionid(sessionid);
	}
};
