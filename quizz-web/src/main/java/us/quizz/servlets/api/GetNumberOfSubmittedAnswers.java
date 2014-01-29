package us.quizz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.UserAnswerRepository;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class GetNumberOfSubmittedAnswers extends HttpServlet {
	
	UserAnswerRepository userAnswerRepository;
	
	@Inject
	public GetNumberOfSubmittedAnswers(UserAnswerRepository userAnswerRepository){
		this.userAnswerRepository = userAnswerRepository;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String quiz = req.getParameter("quiz");
		String userid = req.getParameter("userid");

		Integer answers = userAnswerRepository.getUserAnswers(quiz, userid)
				.size();

		resp.setContentType("application/json;charset=utf-8");
		Gson gson = new Gson();
		Response result = new Response(quiz, answers);
		String json = gson.toJson(result);
		resp.getWriter().println(json);

	}

	class Response {
		String quiz;
		Integer answers;

		Response(String quiz, Integer answers) {
			this.quiz = quiz;
			this.answers = answers;
		}
	}

}
