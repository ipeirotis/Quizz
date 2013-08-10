package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.QuizPerformance;
import com.ipeirotis.crowdquiz.utils.CachePMF;
import com.ipeirotis.crowdquiz.utils.PMF;

/**
 * 
 * Takes as input a userid and a quiz, updates the user scores for the quiz, and then computes
 * the rank of the user within the set of all other users. Finally, it puts the QuizPerformance object
 * in the memcache for quick retrieval.
 * 
 * @author ipeirotis
 *
 */
@SuppressWarnings("serial")
public class UpdateUserQuizStatistics extends HttpServlet {



	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		String quiz = req.getParameter("quiz");
		String userid = req.getParameter("userid");
		if (userid == null || quiz ==null) return;

		QuizPerformance qp = null;
		boolean indatastore = false;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			qp = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(quiz, userid));
			indatastore = true;
		} catch (Exception e) {
			qp = new QuizPerformance(quiz, userid);
		}
		
		qp.computeCorrect();
		if (qp.getTotalanswers()==0) {
			if (indatastore) pm.deletePersistent(qp);
			return;
		} else {
			pm.makePersistent(qp);
		}
		
		qp.computeRank();
		CachePMF.put("qp_"+userid+"_"+quiz, qp);
		pm.makePersistent(qp);
		pm.close();
		
		

	}
	
}
