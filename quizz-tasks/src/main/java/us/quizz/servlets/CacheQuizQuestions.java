package us.quizz.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.QuizesOperations;

@SuppressWarnings("serial")
public class CacheQuizQuestions extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain;charset=utf-8");

		List<Quiz> list = QuizRepository.getQuizzes();

		for (Quiz quiz : list) {
			resp.getWriter().println("Updating quiz: " + quiz.getName());
			QuizesOperations.getNextQuizQuestions(quiz.getQuizID(), 10);
		}
	}
}