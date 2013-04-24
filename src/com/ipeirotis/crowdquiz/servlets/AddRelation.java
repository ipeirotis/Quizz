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

	class CompletionsEntryBean {

		private String	mid;
		private Long		coverage;
		private Long		filled_entities;
		private Long		unsupported_entities;
		private Long		empty_entities;
		private Double	empty_weight;
		private Double	total_weight;
		private Double	filled_weight;

		public String getMid() {

			return mid;
		}

		public Long getCoverage() {

			return coverage;
		}

		public Long getFilled_entities() {

			return filled_entities;
		}

		public Long getUnsupported_entities() {

			return unsupported_entities;
		}

		public Long getEmpty_entities() {

			return empty_entities;
		}

		public Double getEmpty_weight() {

			return empty_weight;
		}

		public Double getTotal_weight() {

			return total_weight;
		}

		public Double getFilled_weight() {

			return filled_weight;
		}

		public void setMid(String mid) {

			this.mid = mid;
		}

		public void setCoverage(Long coverage) {

			this.coverage = coverage;
		}

		public void setFilled_entities(Long filled_entities) {

			this.filled_entities = filled_entities;
		}

		public void setUnsupported_entities(Long unsupported_entities) {

			this.unsupported_entities = unsupported_entities;
		}

		public void setEmpty_entities(Long empty_entities) {

			this.empty_entities = empty_entities;
		}

		public void setEmpty_weight(Double empty_weight) {

			this.empty_weight = empty_weight;
		}

		public void setTotal_weight(Double total_weight) {

			this.total_weight = total_weight;
		}

		public void setFilled_weight(Double filled_weight) {

			this.filled_weight = filled_weight;
		}

	}

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

			Question q = new Question(relation, text, freebaseattr, freebasetype, blobKey);
			PersistenceManager pm = PMF.get().getPersistenceManager();
			pm.makePersistent(q);
			pm.close();

			BlobstoreInputStream is = new BlobstoreInputStream(blobKey);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			
			CsvToBean<CompletionsEntryBean> csv = new CsvToBean<CompletionsEntryBean>();
			CSVReader reader = new CSVReader(in);
			ColumnPositionMappingStrategy<CompletionsEntryBean> strat = new ColumnPositionMappingStrategy<CompletionsEntryBean>();
			strat.captureHeader(reader);

			
			List<CompletionsEntryBean> list = csv.parse(strat, reader);

			Queue queue = QueueFactory.getDefaultQueue();
			for (CompletionsEntryBean ce : list) {
				queue.add(Builder.withUrl("/addEntity")
						.param("relation", relation)
						.param("freebaseid", ce.getMid())
						.param("emptyweight", ce.getEmpty_weight().toString())
						.method(TaskOptions.Method.GET));
			}

			/*
			 * Scanner scanner = new Scanner(is);
			 * while(scanner.hasNextLine()){
			 * String s = scanner.nextLine();
			 * String[] entries = s.split(",");
			 * String mid = entries[0];
			 * String weight = entries[5];
			 * if (mid.equals("mid")) continue; // skip first line
			 * queue.add(Builder.withUrl("/addEntity").param("relation", relation).param("freebaseid",
			 * mid).param("emptyweight", weight).method(TaskOptions.Method.GET));
			 * }
			 * scanner.close();
			 */

		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);

		}
	}

}
