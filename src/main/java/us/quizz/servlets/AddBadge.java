package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Badge;
import us.quizz.utils.Helper;
import us.quizz.utils.PMF;

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
