package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Badge;
import us.quizz.repository.BadgeRepository;
import us.quizz.utils.Helper;
import us.quizz.utils.ServletUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class AddBadge extends HttpServlet {
	
	private BadgeRepository badgeRepository;
	
	@Inject
	public AddBadge(BadgeRepository badgeRepository){
		this.badgeRepository = badgeRepository;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		ServletUtils.ensureParameters(req, "name");
		String badgename = req.getParameter("name").trim();
		String shortname = req.getParameter("sname").trim();

		Badge badge = new Badge(badgename, shortname);
		badgeRepository.singleMakePersistent(badge);

		resp.setContentType("text/plain");
		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/badges/";
		resp.sendRedirect(url);
	}
}
