package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.ads.adwords.jaxws.v201302.cm.Campaign;
import com.google.gson.Gson;
import com.ipeirotis.crowdquiz.ads.CampaignManagement;
import com.ipeirotis.crowdquiz.entities.EntityQuestion;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.servlets.AddUserEntry.Response;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddCampaign extends HttpServlet {

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

			String budgetParam = req.getParameter("budget");
			Integer dailyBudget = null;
			if (budgetParam != null) {
				dailyBudget = Integer.parseInt(budgetParam);
			} else {
				return;
			}

			CampaignManagement service = new CampaignManagement();
			String campaignName = relation;
			Campaign campaign = service.createCampaign(campaignName, dailyBudget);
			Long campaignId = service.publishCampaign(campaign);
			
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Question q;
			try {
				q = pm.getObjectById(Question.class, Question.generateKeyFromID(relation));
			} catch (Exception e) {
				return;
			}
			
			q.setCampaignid(campaignId);
			pm.makePersistent(q);
			pm.close();

			
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);

		}
	}
}
