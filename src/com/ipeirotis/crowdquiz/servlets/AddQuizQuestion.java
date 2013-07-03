package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddQuizQuestion extends HttpServlet {

	private HttpServletResponse	r;

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.quizz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		r = resp;
		r.setContentType("text/plain");

		try {
			String relation = req.getParameter("relation");
			if (relation != null) {
				resp.getWriter().println("Adding Relation: " + relation);

			} else {
				return;
			}

			String mid = req.getParameter("mid");
			if (mid != null) {
				resp.getWriter().println("Entity ID: " + mid);
			} else {
				return;
			}

			Double weight = Double.parseDouble(req.getParameter("weight"));
			if (weight != null) {
				resp.getWriter().println("Weight: " + weight);
			} else {
				return;
			}

			QuizQuestion q = new QuizQuestion(relation, mid, weight);

			PersistenceManager pm = PMF.get().getPersistenceManager();
			pm.makePersistent(q);
			pm.close();
			
			
			

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
