package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroup;
import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupAd;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.ads.CampaignManagement;
import com.ipeirotis.crowdquiz.entities.Quiz;
import com.ipeirotis.crowdquiz.entities.QuizQuestion;
import com.ipeirotis.crowdquiz.utils.FreebaseSearch;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddAdGroup extends HttpServlet {

	private HttpServletResponse	r;

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.AddAdGroup");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		r = resp;

		r.setContentType("application/json");

		try {
			String relation = req.getParameter("relation");
			if (relation != null) {
				// resp.getWriter().println("Adding ad campaign for relation: " + relation);

			} else {
				return;
			}
			
			String mid = req.getParameter("mid");
			if (mid != null) {
				// resp.getWriter().println("Adding ad campaign for relation: " + relation);

			} else {
				return;
			}
			
			String cpcbid = req.getParameter("cpcbid");
			if (cpcbid != null) {
				// resp.getWriter().println("Adding ad campaign for relation: " + relation);

			} else {
				return;
			}
			
			String keywords = req.getParameter("keywords");
			if (keywords != null) {
				// resp.getWriter().println("Adding ad campaign for relation: " + relation);

			} else {
				return;
			}
			
			String adheadline = req.getParameter("adheadline");
			if (adheadline != null) {
				//resp.getWriter().println("adText: " + adheadline);
			} else {
				// return;
			}
			
			String adline1 = req.getParameter("adline1");
			if (adline1 != null) {
				//resp.getWriter().println("adText: " + adline1);
			} else {
				// return;
			}
			
			String adline2 = req.getParameter("adline2");
			if (adline2 != null) {
				//resp.getWriter().println("adText: " + adline2);
			} else {
				// return;
			}
			
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Quiz q;
			Long campaignId = null;
			
				try {
					q = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
					campaignId  = q.getCampaignid();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				} 
				pm.close();
				
				// All relations (should) have a corresponding ad campaign. 
				// If we get a null, we just put the task back in the queue
				if (campaignId == null) {
					Queue queueAdgroup  = QueueFactory.getQueue("adgroup");
					long delay = 10; // in seconds
					long etaMillis = System.currentTimeMillis() + delay * 1000L;
					queueAdgroup.add(Builder.withUrl("/addAdGroup")
							.param("relation", relation)
							.param("mid", mid)
							.param("cpcbid", cpcbid)
							.param("keywords", keywords)
								.param("adheadline", adheadline)
								.param("adline1", adline1)
								.param("adline2", adline2)
							.method(TaskOptions.Method.GET)
							.etaMillis(etaMillis));
					return;
				}

			



			CampaignManagement service = new CampaignManagement();

			String midName = FreebaseSearch.getFreebaseAttribute(mid, "name");
			String adGroupName = midName;
			
			AdGroup adgroup = service.createAdgroup(adGroupName, campaignId, Double.parseDouble(cpcbid));
			Long adgroupId = service.publishAdgroup(adgroup);
			
			String[] keyword = keywords.split(",");
			for (String k : keyword) {
				String bidKeyword = midName.toLowerCase() + " " +  k.trim().toLowerCase();
				service.addKeyword(bidKeyword.replaceAll("[^A-Za-z0-9 ]", " "), adgroupId);
			}
			
		
			String displayURL = "http://crowd-power.appspot.com";
			String targetURL = "http://crowd-power.appspot.com/askQuestion.jsp?mid="+URLEncoder.encode(mid, "UTF-8")+"&relation="+URLEncoder.encode(relation, "UTF-8");
			AdGroupAd ad = service.createTextAd(adheadline, adline1, adline2, displayURL, targetURL, adgroupId);
			Long textAdId = service.publishTextAd(ad);
			
			pm = PMF.get().getPersistenceManager();
			QuizQuestion eq = null;
				try {
					eq = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(relation, mid));
					eq.setAdTextId(textAdId);
					eq.setAdGroupId(adgroupId);
					pm.makePersistent(eq);
				} catch (Exception e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				} 
				pm.close();
			

			
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);

		}
	}
}
