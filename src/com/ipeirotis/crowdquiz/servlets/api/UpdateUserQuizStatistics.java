package com.ipeirotis.crowdquiz.servlets.api;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizPerformanceRepository;

import com.ipeirotis.crowdquiz.entities.QuizPerformance;

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

		QuizPerformance qp = QuizPerformanceRepository.getQuizPerformance(quiz, userid);
		boolean indatastore = (qp!=null);
		
		if (!indatastore) {
			qp = new QuizPerformance(quiz, userid);
		}	
		
		qp.computeCorrect();
		
		if (qp.getTotalanswers()==0) {
			if (indatastore) QuizPerformanceRepository.deleteQuizPerformance(qp);
			return;
		}
		
		qp.computeRank();
		QuizPerformanceRepository.storeQuizPerformance(qp);
	}
	
}
