package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.Badge;
import com.ipeirotis.crowdquiz.utils.Helper;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddBadge extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Utils.ensureParameters(req, "name");
		String badgename = req.getParameter("name").trim();
		String shortname = req.getParameter("sname").trim();

		Badge badge = new Badge(badgename, shortname);
		PMF.singleMakePersistent(badge);

		resp.setContentType("text/plain");
		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/badges/";
		resp.sendRedirect(url);
	}
}
