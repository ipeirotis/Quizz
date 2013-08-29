package com.ipeirotis.crowdquiz.servlets;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddQuizAdCampaign extends HttpServlet {

	private HttpServletResponse	r;
	final static Logger					logger						= Logger.getLogger("com.ipeirotis.adcrowdkg");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		r = resp;
		r.setContentType("text/plain");
		Utils.ensureParameters(req, "relation", "name", "text", "fbtype");

		try {
			String relation = req.getParameter("relation");
			resp.getWriter().println("Adding Relation: " + relation);
			
			String name = req.getParameter("name");
			resp.getWriter().println("Name: " + name);
			
			String text = req.getParameter("text");
			resp.getWriter().println("Question Text: " + text);

			String freebasetype = req.getParameter("fbtype");
			resp.getWriter().println("Freebase Type: " + freebasetype);

			/*
			String freebaseattr = req.getParameter("fbattr");
			if (freebaseattr != null) {
				resp.getWriter().println("Freebase Property: " + freebaseattr);
			} else {
				return;
			}
			
			String fbelement = req.getParameter("fbelement");
			if (fbelement != null) {
				resp.getWriter().println("Freebase Element: " + fbelement);
			} else {
				// return;
			}
			*/

			String budget = req.getParameter("budget");
			if (budget != null) {
				resp.getWriter().println("Budget: " + budget);
			} else {
				// return;
			}
			
			String cpcbid = req.getParameter("cpcbid");
			if (cpcbid != null) {
				resp.getWriter().println("CPC bid: " + cpcbid);
			} else {
				// return;
			}
			
			String keywords = req.getParameter("keywords");
			if (keywords != null) {
				resp.getWriter().println("AdKeywords: " + keywords);
			} else {
				// return;
			}
			
			String adheadline = req.getParameter("adheadline");
			if (adheadline != null) {
				resp.getWriter().println("adText: " + adheadline);
			} else {
				// return;
			}
			
			String adline1 = req.getParameter("adline1");
			if (adline1 != null) {
				resp.getWriter().println("adText: " + adline1);
			} else {
				// return;
			}
			
			String adline2 = req.getParameter("adline2");
			if (adline2 != null) {
				resp.getWriter().println("adText: " + adline2);
			} else {
				// return;
			}
			
			
			Quiz q = new Quiz(name, relation, text);
			
			
			
			PersistenceManager pm = PMF.getPM();
			pm.makePersistent(q);
			pm.close();
			
			Queue queueAdCampaign = QueueFactory.getQueue("adcampaign");
			
			queueAdCampaign.add(Builder.withUrl("/addCampaign")
					.param("relation", relation)
					.param("budget", budget)
					.method(TaskOptions.Method.GET));
			

			
			Queue queueAdgroup  = QueueFactory.getQueue("adgroup");
			
			for (;;) {
				
				// We introduce a delay of a few secs to allow the ad campaign
				// to be created and for the entries to be uploaded and stored
				long delay = 10; // in seconds
				long etaMillis = System.currentTimeMillis() + delay * 1000L;
				queueAdgroup.add(Builder.withUrl("/addAdGroup")
						.param("relation", relation)
						.param("mid", "mid")
						.param("cpcbid", cpcbid)
						.param("keywords", keywords)
						.param("adheadline", adheadline)
						.param("adline1", adline1)
						.param("adline2", adline2)
						.method(TaskOptions.Method.GET)
						.etaMillis(etaMillis));
				

				
			}
			

			


		} catch (Exception e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
		}
	}

}
