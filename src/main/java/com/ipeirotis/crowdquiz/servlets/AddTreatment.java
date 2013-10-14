package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.Treatment;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddTreatment extends HttpServlet {

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		Utils.ensureParameters(req, "name");
		try {

			String name = req.getParameter("name").trim();

			Double p = 0.0;
			String prob = req.getParameter("probability");
			try {
				p = Double.parseDouble(prob);
			} catch (Exception e) {
				resp.getWriter().print("Unable to parse probability value: '"+prob+"'\n");
				resp.getWriter().print("Assigning default probability value of 0.0\n");
			}
			
			Treatment treatment = new Treatment(name, p);
			PMF.singleMakePersistent(treatment);
			
			resp.getWriter().println("OK");
			resp.sendRedirect("/admin/treatments/");
			
			// TODO: Go over all users and add the new condition in the Experiment entity
			// Similarly, when we implement the "deleteTreatment"
			

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}
}
