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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Builder;
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

			Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
			BlobKey blobKey = blobs.get("myFile");

			Question q = new Question(name, relation, text, freebaseattr, freebasetype, blobKey);
			PersistenceManager pm = PMF.get().getPersistenceManager();
			pm.makePersistent(q);
			pm.close();

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

			Queue queue = QueueFactory.getDefaultQueue();
			for (CompletionsEntryBean ce : list) {
				queue.add(Builder.withUrl("/addEntity")
						.param("relation", relation)
						.param("freebaseid", ce.getMid())
						.param("emptyweight", ce.getEmpty_weight().toString())
						.method(TaskOptions.Method.GET));
			}

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
		}
	}

}
