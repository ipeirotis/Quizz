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
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UpdateAllQuestionStatistics extends HttpServlet {
	
	private QuizQuestionRepository quizQuestionRepository;
	
	@Inject
	public UpdateAllQuestionStatistics(QuizQuestionRepository quizQuestionRepository){
		this.quizQuestionRepository = quizQuestionRepository;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Queue queue = QueueFactory.getQueue("updateUserStatistics");

		List<Question> questions = quizQuestionRepository
				.getQuizQuestions();

		for (Question question : questions) {
			queue.add(Builder
					.withUrl("/api/updateQuestionStatistics")
					.param("questionID", question.getID().toString())
					.method(TaskOptions.Method.POST));
		}
	}

}
