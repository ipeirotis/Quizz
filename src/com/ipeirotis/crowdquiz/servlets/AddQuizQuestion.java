package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddQuizQuestion extends HttpServlet {

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.quizz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("text/plain");
		Utils.ensureParameters(req, "relation", "mid", "name", "weight");

		try {
			String relation = req.getParameter("relation").trim();
			resp.getWriter().println("Adding Relation: " + relation);

			String mid = req.getParameter("mid").trim();
			resp.getWriter().println("Entity ID: " + mid);
			
			String name = req.getParameter("name").trim();
			resp.getWriter().println("Entity name: " + name);
			
			Double weight = Double.parseDouble(req.getParameter("weight"));
			resp.getWriter().println("Weight: " + weight);

			QuizQuestion q = new QuizQuestion(relation, mid, name, weight);
			PMF.singleMakePersistent(q);

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
