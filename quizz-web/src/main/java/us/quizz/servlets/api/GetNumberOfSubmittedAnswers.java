package us.quizz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.UserAnswerRepository;

import com.google.gson.Gson;

@SuppressWarnings("serial")
public class GetNumberOfSubmittedAnswers extends HttpServlet {

	class Response {
		String quiz;
		Integer answers;

		Response(String quiz, Integer answers) {
			this.quiz = quiz;
			this.answers = answers;
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String quiz = req.getParameter("quiz");
		String userid = req.getParameter("userid");

		Integer answers = UserAnswerRepository.getUserAnswers(quiz, userid)
				.size();

		resp.setContentType("application/json;charset=utf-8");
		Gson gson = new Gson();
		Response result = new Response(quiz, answers);
		String json = gson.toJson(result);
		resp.getWriter().println(json);

	}

}
