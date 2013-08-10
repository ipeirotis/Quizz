package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;

import com.ipeirotis.crowdquiz.entities.GoldAnswer;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddGoldAnswer extends HttpServlet {

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
			
			String answer = req.getParameter("answer");
			if (answer != null) {

			} else {
				return;
			}

			// We only add the gold question, if there is a corresponding quizquestion.
			// Otherwise, we ignore the addition
			QuizQuestion qq = QuizQuestionRepository.getQuizQuestion(relation, mid);
			if (qq==null) return;
			qq.setHasGoldAnswer(true);
			QuizQuestionRepository.storeQuizQuestion(qq);
			GoldAnswer ga = new GoldAnswer(relation, mid, answer);
			
			
	
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.makePersistent(ga);
			} catch (Exception e) {

			} finally {
				pm.close();
			}
			
			

			resp.getWriter().println("OK");

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
