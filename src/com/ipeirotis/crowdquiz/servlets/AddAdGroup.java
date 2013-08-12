package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizQuestionRepository;
import us.quizz.repository.QuizRepository;

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

@SuppressWarnings("serial")
public class AddAdGroup extends HttpServlet {

	private HttpServletResponse r;

	final static Logger logger = Logger.getLogger("com.ipeirotis.AddAdGroup");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		r = resp;

		r.setContentType("application/json");

		try {
			String relation = req.getParameter("relation");
			if (relation == null) {
				resp.setStatus(422); // 422 (Unprocessable Entity)
				return;
			}

			String mid = req.getParameter("mid");
			if (mid == null) {
				// In this case, we create the "default" ad for the quiz
				// return;
			}

			String cpcbid = req.getParameter("cpcbid");
			if (cpcbid == null) {
				resp.setStatus(422); // 422 (Unprocessable Entity)
				return;
			}

			String keywords = req.getParameter("keywords");
			if (keywords == null) {
				resp.setStatus(422); // 422 (Unprocessable Entity)
				return;
			}

			String adheadline = req.getParameter("adheadline");
			if (adheadline == null) {
				resp.setStatus(422); // 422 (Unprocessable Entity)
				return;
			}

			String adline1 = req.getParameter("adline1");
			if (adline1 == null) {
				resp.setStatus(422); // 422 (Unprocessable Entity)
				return;
			}

			String adline2 = req.getParameter("adline2");
			if (adline2 == null) {
				resp.setStatus(422); // 422 (Unprocessable Entity)
				return;
			}

			Quiz q = QuizRepository.getQuiz(relation);
			Long campaignId = q.getCampaignid();
			
			
			if (campaignId == null) {
				// All relations (should) have a corresponding ad campaign.
				// If we get a null, we just put the task back in the queue
				// and run the call again.
				// This happens either when the Quiz object has not yet 
				// persisted in the datastore the campaignId
				Queue queueAdgroup = QueueFactory.getQueue("adgroup");
				long delay = 10; // in seconds
				long etaMillis = System.currentTimeMillis() + delay * 1000L;
				
				if (mid==null) {
				queueAdgroup.add(Builder.withUrl("/addAdGroup")
						.param("relation", relation)
						.param("cpcbid", cpcbid)
						.param("keywords", keywords)
						.param("adheadline", adheadline)
						.param("adline1", adline1)
						.param("adline2", adline2)
						.method(TaskOptions.Method.POST).etaMillis(etaMillis));
				} else {
					queueAdgroup.add(Builder.withUrl("/addAdGroup")
							.param("relation", relation)
							.param("mid", mid)
							.param("cpcbid", cpcbid)
							.param("keywords", keywords)
							.param("adheadline", adheadline)
							.param("adline1", adline1)
							.param("adline2", adline2)
							.method(TaskOptions.Method.POST).etaMillis(etaMillis));
				}
				resp.setStatus(202); // The request has been accepted for
										// processing, but the processing has
										// not been completed
				return;
			}

			CampaignManagement service = new CampaignManagement();

			String midName = (mid == null) ? "default" : FreebaseSearch	.getFreebaseAttribute(mid, "name");
			String adGroupName = midName;

			AdGroup adgroup = service.createAdgroup(adGroupName, campaignId, Double.parseDouble(cpcbid));
			Long adgroupId = service.publishAdgroup(adgroup);

			String[] keyword = keywords.split(",");
			for (String k : keyword) {
				String bidKeyword = ((mid != null) ? midName.toLowerCase() :"") + " " + k.trim().toLowerCase();
				service.addKeyword(bidKeyword.replaceAll("[^A-Za-z0-9 ]", " "), adgroupId);
			}

			String displayURL = "http://www.quizz.us";
			String targetURL = "http://www.quizz.us/startQuiz?relation=" + URLEncoder.encode(relation, "UTF-8");
			AdGroupAd ad = service.createTextAd(adheadline, adline1, adline2, displayURL, targetURL, adgroupId);
			Long textAdId = service.publishTextAd(ad);

			
			QuizQuestion eq =QuizQuestionRepository.getQuizQuestion(relation, mid);	
			eq.setAdTextId(textAdId);
			eq.setAdGroupId(adgroupId);
			QuizQuestionRepository.storeQuizQuestion(eq);
			

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);

		}
	}
}
