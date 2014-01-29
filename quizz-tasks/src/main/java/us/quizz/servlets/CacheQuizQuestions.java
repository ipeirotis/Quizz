package us.quizz.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class CacheQuizQuestions extends HttpServlet {
	
	private QuizQuestionRepository quizQuestionRepository;
	private QuizRepository quizRepository;
	
	@Inject
	public CacheQuizQuestions(QuizQuestionRepository quizQuestionRepository, 
			QuizRepository quizRepository){
		this.quizQuestionRepository = quizQuestionRepository;
		this.quizRepository = quizRepository;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain;charset=utf-8");

		List<Quiz> list = quizRepository.getQuizzes();

		for (Quiz quiz : list) {
			resp.getWriter().println("Updating quiz: " + quiz.getName());
			quizQuestionRepository.getNextQuizQuestions(quiz.getQuizID(), 10);
		}
	}
}