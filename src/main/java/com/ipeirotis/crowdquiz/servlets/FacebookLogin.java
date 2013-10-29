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
		System.out.println("Logging in");
		User user = null;
		Utils.ensureParameters(req, "fbid", "sessionid", "url");
		String fbid = req.getParameter("fbid");
		String sessionid = req.getParameter("sessionid");
		System.out.println("Before get fb user");
		user = User.getUseridFromFbid(fbid);
		System.out.println("after get fb user");
		if (user == null) {
			System.out.println("Before get cookie user");
			user = User.getUseridFromCookie(req, resp);
			user.setFBID(fbid);
		}
		System.out.println("after get user");
		user.setSessionid(sessionid);
		System.out.println(user.getUserid());
	}
};