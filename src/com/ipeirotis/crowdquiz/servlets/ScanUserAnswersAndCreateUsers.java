package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

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
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserAnswer.class);

		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) query.execute();
		Set<String> userids = new TreeSet<String>();
		for (UserAnswer answer : answers) {
			String userid = answer.getUserid();
			userids.add(userid);
		}
		
		for (String userid : userids) {
			User user = null;
			try {
				user = pm.getObjectById(User.class, User.generateKeyFromID(userid));
			} catch (Exception e) {
				user = new User(userid);
				pm.makePersistent(user);
			}
		}
		
		pm.close();
		
	}

}
