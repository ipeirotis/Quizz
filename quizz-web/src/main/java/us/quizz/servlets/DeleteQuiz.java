package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;
import us.quizz.utils.Helper;
import us.quizz.utils.ServletUtils;

@SuppressWarnings("serial")
public class DeleteQuiz extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/";
		resp.sendRedirect(url);
		ServletUtils.ensureParameters(req, "quizID");

		String quizID = req.getParameter("quizID");
		QuizRepository.deleteQuiz(quizID);
	}

}
