package com.ipeirotis.crowdquiz.servlets;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import com.google.api.ads.adwords.jaxws.v201302.cm.AdGroupAd;
import com.google.api.ads.adwords.jaxws.v201302.cm.Campaign;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
import com.ipeirotis.crowdquiz.ads.CampaignManagement;
import com.ipeirotis.crowdquiz.entities.Question;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class AddRelation extends HttpServlet {

	private HttpServletResponse	r;
	private BlobstoreService		blobstoreService	= BlobstoreServiceFactory.getBlobstoreService();

	final static Logger					logger						= Logger.getLogger("com.ipeirotis.adcrowdkg");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		r = resp;

		r.setContentType("text/plain");

		try {
			String relation = req.getParameter("relation");
			if (relation != null) {
				resp.getWriter().println("Adding Relation: " + relation);
			} else {
				return;
			}
			
			String name = req.getParameter("name");
			if (name != null) {
				resp.getWriter().println("Name: " + name);
			} else {
				return;
			}

			String text = req.getParameter("text");
			if (text != null) {
				resp.getWriter().println("Question Text: " + text);
			} else {
				return;
			}

			String freebasetype = req.getParameter("fbtype");
			if (freebasetype != null) {
				resp.getWriter().println("Freebase Type: " + freebasetype);
			} else {
				return;
			}

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
			
			
			Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
			BlobKey blobKey = blobs.get("myFile");

			Question q = new Question(name, relation, text, freebaseattr, fbelement, freebasetype, blobKey);
			

			
			
			PersistenceManager pm = PMF.get().getPersistenceManager();
			pm.makePersistent(q);
			pm.close();
			
			Queue queueAdCampaign = QueueFactory.getQueue("adcampaign");
			
			queueAdCampaign.add(Builder.withUrl("/addCampaign")
					.param("relation", relation)
					.param("budget", budget)
					.method(TaskOptions.Method.GET));
			

			BlobstoreInputStream is = new BlobstoreInputStream(blobKey);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			CsvToBean<CompletionsEntryBean> csv = new CsvToBean<CompletionsEntryBean>();
			CSVReader reader = new CSVReader(in);
			String [] header = reader.readNext();

			ColumnPositionMappingStrategy<CompletionsEntryBean> strat = new ColumnPositionMappingStrategy<CompletionsEntryBean>();
			strat.setType(CompletionsEntryBean.class);
			strat.captureHeader(reader);
			strat.setColumnMapping(header);

			List<CompletionsEntryBean> list = csv.parse(strat, reader);


			Queue queueEntities = QueueFactory.getQueue("entities");
			Queue queueAdgroup  = QueueFactory.getQueue("adgroup");
			
			for (CompletionsEntryBean ce : list) {
				queueEntities.add(Builder.withUrl("/addEntity")
						.param("relation", relation)
						.param("freebaseid", ce.getMid())
						.param("emptyweight", ce.getEmpty_weight().toString())
						.method(TaskOptions.Method.GET));
				
				// We introduce a delay of a few secs to allow the ad campaign
				// to be created and for the entries to be uploaded and stored
				long delay = 10; // in seconds
				long etaMillis = System.currentTimeMillis() + delay * 1000L;
				queueAdgroup.add(Builder.withUrl("/addAdGroup")
						.param("relation", relation)
						.param("mid", ce.getMid())
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
