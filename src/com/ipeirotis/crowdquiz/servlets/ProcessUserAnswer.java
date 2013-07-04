package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.GoldAnswer;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.Helper;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class ProcessUserAnswer extends HttpServlet {

	class Response {

		String				url;
		String				correctAnswer;
		List<String>	crowdAnswers;

		Response(String url, String correct, List<String> crowd) {

			this.url = url;
			this.correctAnswer = correct;
			this.crowdAnswers = crowd;
		}
	}


	final static Logger					logger	= Logger.getLogger("com.ipeirotis.crowdquiz");
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String relation = req.getParameter("relation");
		String userid = Helper.getUseridFromCookie(req, resp);
		
	String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
	String nextURL = baseURL + getNextURL(relation, userid, null);
	
	resp.sendRedirect(nextURL);
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("application/json");

		String userid = Helper.getUseridFromCookie(req, resp);
		String relation = req.getParameter("relation");
		String mid = req.getParameter("mid");
		String useranswer = req.getParameter("useranswer");
		String action = req.getParameter("action");
		String ipAddress = req.getRemoteAddr();
		String browser = req.getHeader("User-Agent");
		String referer = req.getHeader("Referer");
		Long timestamp = (new Date()).getTime();

		Queue queue = QueueFactory.getQueue("answers");
		queue.add(Builder.withUrl("/addUserAnswer").
				param("relation", relation).
				param("userid", userid).
				param("action", action).
				param("mid", mid).
				param("useranswer", useranswer).
				param("browser", browser).
				param("ipAddress", ipAddress).
				param("referer", referer).
				param("timestamp", timestamp.toString()).
				method(TaskOptions.Method.POST));

		
		Gson gson = new Gson();
		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		String nextURL = baseURL + getNextURL(relation, userid, mid);
		List<String> answers = getCrowdAnswers(relation, userid, mid);
		String goldaanswer = getGoldAnswer(relation, mid);
		Response result = new Response(nextURL, goldaanswer, answers);
		String json = gson.toJson(result);
		System.out.println(json);
		resp.getWriter().println(json);

	}

	private String getGoldAnswer(String relation, String mid) {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		GoldAnswer ga;
		try {
			ga = pm.getObjectById(GoldAnswer.class, GoldAnswer.generateKeyFromID(relation, mid));
		} catch (Exception e) {
			ga = null;
		}
		pm.close();
		if (ga!=null) {
			return ga.getAnswer();
		} else {
			return null;
		}
	}


	/**
	 * Returns the next question for the user. Checks all the previously given answers by the user
	 * to avoid returning a question for which we already have an answer from the user. The parameter
	 * justAddedMid ensures that we do not return the currently asked question, even if the relation
	 * has not persisted in the datastore yet.
	 * 
	 * 
	 * @param relation
	 * @param userid
	 * @param justAddedMid
	 * @param pm
	 * @return
	 */
	private String getNextURL(String relation, String userid, String justAddedMid) {

		String nextURL = "/listEntities.jsp?relation=" + relation;

		PersistenceManager	pm = PMF.get().getPersistenceManager();
		
		String query = "SELECT FROM " + QuizQuestion.class.getName() + " WHERE relation=='" + relation + "'"
				+ "  ORDER BY weight DESC";
		System.out.println(query);
		@SuppressWarnings("unchecked")
		List<QuizQuestion> questions = (List<QuizQuestion>) pm.newQuery(query).execute();

		String queryGivenAnswers = "SELECT FROM " + UserAnswer.class.getName() + " WHERE userid=='" + userid
				+ "' && relation=='" + relation + "'";
		System.out.println(queryGivenAnswers);
		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) pm.newQuery(queryGivenAnswers).execute();
		pm.close();
		Set<String> entries = new HashSet<String>();
		if (justAddedMid!=null) {
			entries.add(justAddedMid);
		}
		for (UserAnswer ue : answers) {
			entries.add(ue.getMid());
		}

		if (!questions.isEmpty()) {
			for (QuizQuestion q : questions) {
				String fmid = q.getFreebaseEntityId();
				if (entries.contains(fmid))
					continue;
				nextURL = "/askQuestion.jsp?relation=" + relation + "&mid=" + fmid;
				break;
			}
		}
		return nextURL;
	}

	private List<String> getCrowdAnswers(String relation, String userid, String mid) {

		List<String> result = new ArrayList<String>();

		PersistenceManager	pm = PMF.get().getPersistenceManager();
		
		String queryGivenAnswers = "SELECT from " + UserAnswer.class.getName() + " where mid=='" + mid + "' && relation=='"
				+ relation + "'";
		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) pm.newQuery(queryGivenAnswers).execute();
		pm.close();

		for (UserAnswer ue : answers) {
			if (ue.getUserid().equals(userid)) {
				continue;
			}
			result.add(ue.getUseranswer());
		}
		return result;

	}

}
