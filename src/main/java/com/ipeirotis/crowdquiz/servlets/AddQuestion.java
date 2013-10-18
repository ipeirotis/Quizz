package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddQuestion extends HttpServlet {

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.quizz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("application/json");
		Utils.ensureParameters(req, "quizID", "text", "weight");

		try {
			Map<String, Object> response = new HashMap<String, Object>();
			String quizID = req.getParameter("quizID").trim();
			response.put("quizID", quizID);
			
			String name = req.getParameter("text").trim();
			response.put("text", name);
			
			Double weight = Double.parseDouble(req.getParameter("weight"));
			response.put("weigth", weight);

			Question q = new Question(quizID, name, weight);
			PMF.singleMakePersistent(q);
			response.put("questionID",  q.getID());
			
			resp.getWriter().println(new Gson().toJson(response));

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
