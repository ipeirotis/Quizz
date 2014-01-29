package us.quizz.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateCountStatistics extends HttpServlet {

	private QuizRepository quizRepository;
	
	@Inject
	public UpdateCountStatistics(QuizRepository quizRepository){
		this.quizRepository = quizRepository;
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain;charset=utf-8");

		Queue queue = QueueFactory.getQueue("default");
		List<Quiz> list = quizRepository.getQuizzes();

		for (Quiz quiz : list) {
			resp.getWriter().println("Updating quiz: " + quiz.getName());

			queue.add(Builder.withUrl("/api/getQuizCounts")
					.param("quizID", quiz.getQuizID()).param("cache", "no")
					.method(TaskOptions.Method.GET));

		}
	}
}
