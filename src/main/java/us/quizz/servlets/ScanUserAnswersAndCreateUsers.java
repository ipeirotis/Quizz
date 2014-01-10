package us.quizz.servlets;

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
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.utils.PMF;

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;

@SuppressWarnings("serial")
public class ScanUserAnswersAndCreateUsers extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		
		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		queue.add(Builder.withUrl("/scanUserAnswersAndCreateUsers")
				.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
				.method(TaskOptions.Method.POST));
		
		response.getWriter().println("Process started.");
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(UserAnswer.class);


		Queue queue = QueueFactory.getQueue("updateUserStatistics");
		
		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) query.execute();
		Set<String> userids = new TreeSet<String>();
		for (UserAnswer answer : answers) {
			String userid = answer.getUserid();
			String quizid = answer.getQuizID();
			userids.add(userid);
			
			queue.add(Builder.withUrl("/api/updateUserQuizStatistics")
					.header("Host", BackendServiceFactory.getBackendService().getBackendAddress("backend"))
					.param("userid", userid)
					.param("quizID", quizid)
					.method(TaskOptions.Method.POST));
		}
		
		for (String userid : userids) {
			User user = null;
			try {
				user = pm.getObjectById(User.class, User.generateKeyFromID(userid));
			} catch (Exception e) {
				user = new User(userid);
				Experiment exp = new Experiment();
				exp.assignTreatments();
				user.setExperiment(exp);
				pm.makePersistent(user);
			}
		}
		
		pm.close();
		
	}

}
