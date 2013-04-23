package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.EntityQuestion;
import com.ipeirotis.crowdquiz.entities.UserEntry;
import com.ipeirotis.crowdquiz.utils.PMF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class AddUserEntry extends HttpServlet {

	class Response {
		
		String url;
		String correctAnswer;
		List<String> crowdAnswers;
		Response(String url, String correct, List<String> crowd) {
			this.url = url;
			this.correctAnswer = correct;
			this.crowdAnswers = crowd;
		}
	}
	
	private HttpServletResponse	r;
	
	final static Logger logger = Logger.getLogger("com.ipeirotis.adcrowdkg"); 
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		
		r = resp;

		r.setContentType("application/json");
		
		try {
			String useranswer = req.getParameter("useranswer");
			if (useranswer != null) {
				//resp.getWriter().println("User's answer: " + useranswer);
				
			} else {
				return;
			}

			String relation = req.getParameter("relation");
			if (relation != null) {
				//resp.getWriter().println("Relation: " + relation);
			} else {
				return;
			}
			
			String freebaseanswer = req.getParameter("freebaseanswer");
			if (freebaseanswer != null) {
				//resp.getWriter().println("Answer from Freebase: " + freebaseanswer);
			} else {
				return;
			}
			
			// This is a hack to convert energy from KJ (what is returned by Freebase) to calories
			// when we are crowdsourcing food calories
			if (relation.equals("kc:/food/food:energy")) {
				try {
					int kj = Integer.parseInt(freebaseanswer);
					freebaseanswer = (""+Math.round(kj/4.2));
				} catch (Exception e) {
					freebaseanswer = "";
				}
			} 
			

			
			String mid = req.getParameter("mid");
			if (mid != null) {
				//resp.getWriter().println("Freebase Entity ID: " + mid);
			} else {
				return;
			}
			
			String userid = req.getParameter("userid");
			if (userid != null) {
				//resp.getWriter().println("User ID: " + userid);
			} else {
				return;
			}


			UserEntry ue = new UserEntry(userid, relation, mid, useranswer, freebaseanswer) ;
			
			PersistenceManager pm = PMF.get().getPersistenceManager();
			pm.makePersistent(ue);
			String nextURL = baseURL + getNextURL(relation, userid, mid, pm);
			List<String> answers = getCrowdAnswers(relation, userid, mid, pm);
			pm.close();
			
			Gson gson = new Gson();
			Response result = new Response(nextURL, freebaseanswer, answers);
			String json = gson.toJson(result);
			System.out.println(json);
			resp.getWriter().println(json);
			

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
			
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
	private String getNextURL(String relation, String userid, String justAddedMid, PersistenceManager pm) {

		String nextURL =  "/listEntities.jsp?relation=" + relation;
		
		String query = "select from " + EntityQuestion.class.getName() + " where relation=='"+relation+	"'" 
				+"  order by emptyweight DESC";
			System.out.println(query);
			List<EntityQuestion> questions = (List<EntityQuestion>) pm.newQuery(query).execute();
			
			String queryGivenAnswers = "SELECT from "  + UserEntry.class.getName() + 
					" where userid=='" + userid +"' && relation=='"+relation+	"'";
					System.out.println(queryGivenAnswers);
			List<UserEntry> answers = (List<UserEntry>) pm.newQuery(queryGivenAnswers).execute();
			Set<String> entries = new HashSet<String>();
			entries.add(justAddedMid);
			for (UserEntry ue: answers) {
				entries.add(ue.getMid());
			}
				
			
			
			if (!questions.isEmpty()) {
				for (EntityQuestion q: questions) {
					String fmid = q.getFreebaseEntityId();
					if (entries.contains(fmid)) continue;
					nextURL = "/askQuestion.jsp?relation=" + relation + "&mid="+fmid;
					break;
				}
			}
		return nextURL;
	}
	
	private List<String> getCrowdAnswers(String relation,  String userid,  String mid, PersistenceManager pm) {
		
		List<String> result = new ArrayList<String>();
		
		String queryGivenAnswers = "SELECT from "  + UserEntry.class.getName() + 
				" where mid=='" + mid +"' && relation=='"+relation+	"'";
				//System.out.println(queryGivenAnswers);
		List<UserEntry> answers = (List<UserEntry>) pm.newQuery(queryGivenAnswers).execute();

		for (UserEntry ue: answers) {
			if (ue.getUserid().equals(userid)) continue;
			
			result.add(ue.getUseranswer());
			
		}
		
		return result;
		
	}
	
}
