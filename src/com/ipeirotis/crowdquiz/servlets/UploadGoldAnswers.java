package com.ipeirotis.crowdquiz.servlets;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.ipeirotis.crowdquiz.utils.Helper;

@SuppressWarnings("serial")
public class UploadGoldAnswers extends HttpServlet {

	private BlobstoreService		blobstoreService	= BlobstoreServiceFactory.getBlobstoreService();

	final static Logger					logger= Logger.getLogger("com.ipeirotis.adcrowdkg");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/manage/";
		resp.sendRedirect(url); 

		try {
			String relation = req.getParameter("relation");
			if (relation == null) {
				return;
			}
			
			@SuppressWarnings("deprecation")
			Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
			BlobKey blobKey = blobs.get("gold_file");

			BlobstoreInputStream is = new BlobstoreInputStream(blobKey);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			CsvToBean<GoldAnswerBean> csv = new CsvToBean<GoldAnswerBean>();
			CSVReader reader = new CSVReader(in);
			String [] header = reader.readNext();

			ColumnPositionMappingStrategy<GoldAnswerBean> strat = new ColumnPositionMappingStrategy<GoldAnswerBean>();
			strat.setType(GoldAnswerBean.class);
			strat.captureHeader(reader);
			strat.setColumnMapping(header);

			List<GoldAnswerBean> list = csv.parse(strat, reader);


			Queue queueEntities = QueueFactory.getQueue("answers");
			
			for (GoldAnswerBean ce : list) {
				queueEntities.add(Builder.withUrl("/addGoldAnswer")
						.param("relation", relation)
						.param("mid", ce.getMid())
						.param("answer", ce.getAnswer())
						.method(TaskOptions.Method.POST));
				
			}
			

			


		} catch (Exception e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
		}
	}

}
