package us.quizz.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateAllUserStatistics extends HttpServlet {
	
	private QuizRepository quizRepository;
	private UserRepository userRepository;
	
	@Inject
	public UpdateAllUserStatistics(QuizRepository quizRepository, UserRepository userRepository){
		this.quizRepository = quizRepository;
		this.userRepository = userRepository;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/plain");

		List<Quiz> quizzes = quizRepository.getQuizzes();
		for (Quiz q : quizzes) {

			Queue queue = QueueFactory.getQueue("updateUserStatistics");
			queue.add(Builder
					.withUrl("/api/updateAllUserStatistics")
					.param("quizID", q.getQuizID())
					.method(TaskOptions.Method.POST));

			response.getWriter().println(
					"Process started for quiz:" + q.getName());
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String quiz = req.getParameter("quizID");
		Set<String> userids = userRepository.getUserIDs(quiz);
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		for (String userid : userids) {

			queue.add(Builder
					.withUrl("/api/updateUserQuizStatistics")
					.param("userid", userid).param("quizID", quiz)
					.method(TaskOptions.Method.POST));
		}
	}

}