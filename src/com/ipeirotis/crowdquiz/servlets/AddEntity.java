package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import com.ipeirotis.crowdquiz.entities.EntityQuestion;
import com.ipeirotis.crowdquiz.utils.PMF;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class AddEntity extends HttpServlet {

	private HttpServletResponse	r;
	
	final static Logger logger = Logger.getLogger("com.ipeirotis.adcrowdkg"); 
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		r = resp;

		r.setContentType("text/plain");
		
		try {
			String relation = req.getParameter("relation");
			if (relation != null) {
				resp.getWriter().println("Adding Relation: " + relation);
				
			} else {
				return;
			}

			String freebaseid = req.getParameter("freebaseid");
			if (freebaseid != null) {
				resp.getWriter().println("Freebase Entity ID: " + freebaseid);
			} else {
				return;
			}

			Double emptyweight = Double.parseDouble(req.getParameter("emptyweight"));
			if (emptyweight != null) {
				resp.getWriter().println("Empty weight: " + emptyweight);
			} else {
				return;
			}
			
			EntityQuestion q = new EntityQuestion(relation, freebaseid, emptyweight);

			PersistenceManager pm = PMF.get().getPersistenceManager();
			pm.makePersistent(q);
			pm.close();
			

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
			
		}
	}
}
