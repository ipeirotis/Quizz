package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;

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
	
			// We only add the silver question, if there is a corresponding quizquestion.
			// Otherwise, we ignore the addition
			QuizQuestion qq = QuizQuestionRepository.getQuizQuestion(relation, mid);
			if (qq==null) return;
			qq.setHasSilverAnswers(true);
			QuizQuestionRepository.storeQuizQuestion(qq);
			SilverAnswer sa = new SilverAnswer(relation, mid, answer, source,  probability);
			
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.makePersistent(sa);
			} catch (Exception e) {
				;
			} finally {
				pm.close();
			}
			
			resp.getWriter().println("OK");

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
