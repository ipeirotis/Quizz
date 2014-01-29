package us.quizz.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import us.quizz.repository.QuizRepository;
import us.quizz.utils.Helper;
import us.quizz.utils.ServletUtils;

@SuppressWarnings("serial")
@Singleton
public class DeleteQuiz extends HttpServlet {
	
	private QuizRepository quizRepository;
	
	@Inject
	public DeleteQuiz(QuizRepository quizRepository){
		this.quizRepository = quizRepository;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/";
		resp.sendRedirect(url);
		ServletUtils.ensureParameters(req, "quizID");

		String quizID = req.getParameter("quizID");
		quizRepository.deleteQuiz(quizID);
	}

}
