package us.quizz.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.entities.Quiz;
import us.quizz.repository.QuizRepository;

import com.google.api.ads.adwords.jaxws.v201309.cm.Campaign;
import com.ipeirotis.crowdquiz.ads.CampaignManagement;

@SuppressWarnings("serial")
public class AddCampaign extends HttpServlet {

	final static Logger					logger	= Logger.getLogger("com.ipeirotis.adcrowdkg");

	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doPost(req, resp);
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	
		resp.setContentType("application/json");
		
		Utils.ensureParameters(req, "quizID", "budget");

		try {
			String quizID = req.getParameter("quizID").trim();
			Integer dailyBudget = Integer.parseInt(req.getParameter("budget"));

			CampaignManagement service = new CampaignManagement();
			String campaignName = quizID;
			Campaign campaign = service.createCampaign(campaignName, dailyBudget);
			Long campaignId = service.publishCampaign(campaign);
			
			Quiz q = QuizRepository.getQuiz(quizID);
			q.setCampaignid(campaignId);
			QuizRepository.storeQuiz(q);

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);

		}
	}
}
