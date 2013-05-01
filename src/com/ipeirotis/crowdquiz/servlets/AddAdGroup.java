package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroup;
import com.google.api.ads.adwords.jaxws.v201302.cm.Campaign;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.ads.CampaignManagement;
import com.ipeirotis.crowdquiz.entities.EntityQuestion;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.servlets.AddUserEntry.Response;
import com.ipeirotis.crowdquiz.utils.FreebaseSearch;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddAdGroup extends HttpServlet {

	private HttpServletResponse	r;

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.adcrowdkg");

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
			
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Question q;
			Long campaignId = null;
			
			// TODO: All relations (should) have a corresponding ad campaign. 
			// If we get a null, we may want to just put the task back in the queue
			do {
				try {
					q = pm.getObjectById(Question.class, Question.generateKeyFromID(relation));
					campaignId  = q.getCampaignid();
				} catch (Exception e) {
					return;
				} 
			} while (campaignId == null);
			
			pm.close();


			CampaignManagement service = new CampaignManagement();

			String midName = FreebaseSearch.getFreebaseAttribute(mid, "name");
			String adGroupName = midName;
			
			AdGroup adgroup = service.createAdgroup(adGroupName, campaignId, Double.parseDouble(cpcbid));
			Long adgroupId = service.publishAdgroup(adgroup);
			
			String[] keyword = keywords.split(",");
			for (String k : keyword) {
				String bidKeyword = midName.toLowerCase() + " " +  k.trim().toLowerCase();
				service.addKeyword(bidKeyword, adgroupId);
			}
			
			

			
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);

		}
	}
}
