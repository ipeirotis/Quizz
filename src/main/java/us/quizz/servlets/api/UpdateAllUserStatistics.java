package us.quizz.servlets.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Experiment;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.QuizRepository;
import us.quizz.repository.UserAnswerRepository;
import us.quizz.repository.UserRepository;
import us.quizz.utils.PMF;

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;

@SuppressWarnings("serial")
public class UpdateAllUserStatistics extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		
		List<Quiz> quizzes = QuizRepository.getQuizzes();
		for (Quiz q : quizzes) {
		
			Queue queue = QueueFactory.getQueue("updateUserStatistics");
			queue.add(Builder.withUrl("/api/updateAllUserStatistics")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("quizID", q.getQuizID())
					.method(TaskOptions.Method.POST));
			
			response.getWriter().println("Process started for quiz:"+ q.getName());
		}
	}
	
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		String quiz = req.getParameter("quizID");
		
		Set<String> userids = UserRepository.getUserIDs(quiz);
	
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		
		for (String userid : userids) {
			
			queue.add(Builder.withUrl("/api/updateUserQuizStatistics")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("userid", userid)
					.param("quizID", quiz)
					.method(TaskOptions.Method.POST));
			
			/*
			queue.add(Builder.withUrl("/api/updateUserExperiment")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("userid", userid)
					.method(TaskOptions.Method.POST));
			*/
		}
		
		
		
	}

}
