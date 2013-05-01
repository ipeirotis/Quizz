package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.EntityQuestion;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.entities.UserEntry;
import com.ipeirotis.crowdquiz.utils.FreebaseSearch;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddUserEntry extends HttpServlet {

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

	private HttpServletResponse	r;
	private PersistenceManager pm;

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();

		pm = PMF.get().getPersistenceManager();
		
		r = resp;
		r.setContentType("application/json");
		
		String action = req.getParameter("action");
		
		String userid = getUseridFromCookie(req);

		try {

			String relation = req.getParameter("relation");
			if (relation != null) {
			} else {
				return;
			}

			String mid = req.getParameter("mid");
			if (mid != null) {
			} else {
				return;
			}
			
			/*
			String userid = req.getParameter("userid");
			if (userid != null) {
			} else {
				return;
			}
			*/
			

			String useranswer = req.getParameter("useranswer");
			if (useranswer != null) {

			} else {
				return;
			}

	
			String freebaseanswer = getFreebaseAnswer(relation, mid);
			String ipAddress = req.getRemoteAddr();
			String browser = req.getHeader("User-Agent");
			Long timestamp = (new Date()).getTime();
		
			UserEntry ue = new UserEntry(userid, relation, mid, useranswer);
			ue.setFreebaseanswer(freebaseanswer);
			ue.setBrowser(browser);
			ue.setIpaddress(ipAddress);
			ue.setTimestamp(timestamp);
			ue.setAction(action);
			pm.makePersistent(ue);
			
			String nextURL = baseURL + getNextURL(relation, userid, mid);
			List<String> answers = getCrowdAnswers(relation, userid, mid);
			pm.close();

			Gson gson = new Gson();
			Response result = new Response(nextURL, freebaseanswer, answers);
			String json = gson.toJson(result);
			resp.getWriter().println(json);

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}

	/**
	 * @param req
	 * @return
	 */
	private String getUseridFromCookie(HttpServletRequest req) {

		// Get an array of Cookies associated with this domain
		
		String userid = null;
		Cookie[] cookies = req.getCookies();
	   if (cookies != null) {
		   for (Cookie c: cookies) {
		  	 if (c.getName().equals("username")) {
		  		 userid = c.getValue();
		  		 break;
		  	 }
		   }
	   } 
		
	   if (userid == null) {
	  	 userid = UUID.randomUUID().toString();;
	   }

	Cookie username = new Cookie("username", userid);
	r.addCookie( username );
		return userid;
	}

	/**
	 * @param relation
	 * @param mid
	 * @return
	 * @throws IOException
	 */
	private String getFreebaseAnswer(String relation, String mid) throws IOException {

		Question q = null;
		try {
			q = pm.getObjectById(Question.class, Question.generateKeyFromID(relation));
			} catch (Exception e) {
		    	q = null;
		    	return null;
		} 
		
		String freebaseanswer = FreebaseSearch.getFreebaseTopic(mid, q.getFreebaseAttribute(), q.getFreebaseElement());

		// This is a hack to convert energy from KJ (what is returned by Freebase) to calories
		// when we are crowdsourcing food calories
		if (relation.equals("kc:/food/food:energy")) {
			try {
				int kj = Integer.parseInt(freebaseanswer);
				freebaseanswer = ("" + Math.round(kj / 4.2));
			} catch (Exception e) {
				freebaseanswer = "";
			}
		}
		return freebaseanswer;
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

		String query = "select from " + EntityQuestion.class.getName() + " where relation=='" + relation + "'"
				+ "  order by emptyweight DESC";
		//System.out.println(query);
		@SuppressWarnings("unchecked")
		List<EntityQuestion> questions = (List<EntityQuestion>) pm.newQuery(query).execute();

		String queryGivenAnswers = "SELECT from " + UserEntry.class.getName() + " where userid=='" + userid
				+ "' && relation=='" + relation + "'";
		//System.out.println(queryGivenAnswers);
		@SuppressWarnings("unchecked")
		List<UserEntry> answers = (List<UserEntry>) pm.newQuery(queryGivenAnswers).execute();
		Set<String> entries = new HashSet<String>();
		entries.add(justAddedMid);
		for (UserEntry ue : answers) {
			entries.add(ue.getMid());
		}

		if (!questions.isEmpty()) {
			for (EntityQuestion q : questions) {
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

		String queryGivenAnswers = "SELECT from " + UserEntry.class.getName() + " where mid=='" + mid + "' && relation=='"
				+ relation + "'";
		@SuppressWarnings("unchecked")
		List<UserEntry> answers = (List<UserEntry>) pm.newQuery(queryGivenAnswers).execute();

		for (UserEntry ue : answers) {
			if (ue.getUserid().equals(userid)) {
				continue;
			}

			result.add(ue.getUseranswer());

		}

		return result;

	}

}
