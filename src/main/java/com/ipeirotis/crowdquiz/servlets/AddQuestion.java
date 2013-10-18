package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddQuestion extends HttpServlet {

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.quizz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("text/plain");
		Utils.ensureParameters(req, "quizID", "text", "weight");

		try {
			String quizID = req.getParameter("quizID").trim();
			resp.getWriter().println("Adding to quiz: " + quizID);
			
			String name = req.getParameter("text").trim();
			resp.getWriter().println("Entity name: " + name);
			
			Double weight = Double.parseDouble(req.getParameter("weight"));
			resp.getWriter().println("Weight: " + weight);

			Question q = new Question(quizID, name, weight);
			PMF.singleMakePersistent(q);
			resp.getWriter().println(q.getID());

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
