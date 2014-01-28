package us.quizz.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Question;
import us.quizz.repository.QuizQuestionRepository;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;

@SuppressWarnings("serial")
public class UpdateAllQuestionStatistics extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		queue.add(Builder
				.withUrl("/api/updateAllQuestionStatistics")
				.method(TaskOptions.Method.POST));
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Queue queue = QueueFactory.getQueue("updateUserStatistics");

		List<Question> quizquestions = QuizQuestionRepository
				.getQuizQuestions();

		for (Question quizquestion : quizquestions) {
			queue.add(Builder
					.withUrl("/api/updateQuestionStatistics")
					.param("questionID", quizquestion.getID().toString())
					.method(TaskOptions.Method.POST));
		}
	}

}
