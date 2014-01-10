package us.quizz.servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.utils.Helper;

@SuppressWarnings("serial")
public class AddQuiz extends HttpServlet {

	final static Logger					logger						= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Utils.ensureParameters(req, "quizID", "name");
		String quizID = req.getParameter("quizID").trim();
		resp.getWriter().println("Adding Quiz ID: " + quizID);
		
		String name = req.getParameter("name").trim();
		resp.getWriter().println("Quiz Name: " + name);

		Quiz q = new Quiz(name, quizID);
		QuizRepository.storeQuiz(q);

		resp.setContentType("text/plain");
		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/manage/";
		resp.sendRedirect(url);
	}
}
