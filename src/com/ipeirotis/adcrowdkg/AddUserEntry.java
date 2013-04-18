package com.ipeirotis.adcrowdkg;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class AddUserEntry extends HttpServlet {

	private HttpServletResponse	r;
	
	final static Logger logger = Logger.getLogger("com.ipeirotis.adcrowdkg"); 
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		r = resp;

		r.setContentType("text/plain");
		
		try {
			String useranswer = req.getParameter("useranswer");
			if (useranswer != null) {
				resp.getWriter().println("User's answer: " + useranswer);
				
			} else {
				return;
			}

			String freebaseanswer = req.getParameter("freebaseanswer");
			if (freebaseanswer != null) {
				resp.getWriter().println("Answer from Freebase: " + freebaseanswer);
			} else {
				return;
			}
			
			String relation = req.getParameter("relation");
			if (relation != null) {
				resp.getWriter().println("Relation: " + relation);
			} else {
				return;
			}
			
			String mid = req.getParameter("mid");
			if (mid != null) {
				resp.getWriter().println("Freebase Entity ID: " + mid);
			} else {
				return;
			}
			

			UserEntry ue = new UserEntry(relation, mid, useranswer, freebaseanswer) ;

			PersistenceManager pm = PMF.get().getPersistenceManager();
			pm.makePersistent(ue);
			pm.close();
			

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
			
		}
	}
}
