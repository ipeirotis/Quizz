package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.entities.SilverAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddSilverAnswer extends HttpServlet {

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
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
			
			String source = req.getParameter("source");
			if (source != null) {

			} else {
				return;
			}
			
			String answer = req.getParameter("answer");
			if (answer != null) {

			} else {
				return;
			}

			String prob = req.getParameter("probability");
			Double probability = -1.0;
			if (prob != null) {
				try {
				probability = Double.parseDouble(prob);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			} else {
				return;
			}
	
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				// We only add the gold question, if there is a corresponding quizquestion.
				// Otherwise, we ignore the addition
				QuizQuestion qq = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(relation, mid));
				qq.setHasSilverAnswers(true);
				pm.makePersistent(qq);
				
				SilverAnswer sa = new SilverAnswer(relation, mid, answer, source,  probability);
				pm.makePersistent(sa);
			} catch (Exception e) {
				resp.setStatus(409);
				pm.close();
				return;
			}
			
			
			
			
			
			pm.close();

			resp.getWriter().println("OK");

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
