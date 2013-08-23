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
		Utils.ensureParameters(req, "relation", "mid", "source", "answer");
		try {

			String relation = req.getParameter("relation");
			String mid = req.getParameter("mid");
			String source = req.getParameter("source");
			String answer = req.getParameter("answer");
			
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
				logger.log(Level.WARNING, "Error when making persistent silver answer", e);
			} finally {
				pm.close();
			}
			
			resp.getWriter().println("OK");

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
