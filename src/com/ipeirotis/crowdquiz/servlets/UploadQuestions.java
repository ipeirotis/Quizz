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
public class UploadQuestions extends HttpServlet {

	private BlobstoreService		blobstoreService	= BlobstoreServiceFactory.getBlobstoreService();

	final static Logger					logger= Logger.getLogger("com.ipeirotis.adcrowdkg");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// NOTE: The upload servlets should just return a headers-only redirect and nothing else.
		try {


			String baseURL = Helper.getBaseURL(req);
			String url = baseURL + "/admin/manage/";
			resp.sendRedirect(url); 

			
			String relation = req.getParameter("relation");
			if (relation == null) {
				return;
			} 
			
			@SuppressWarnings("deprecation")
			Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
			BlobKey blobKey = blobs.get("questions_file");

			BlobstoreInputStream is = new BlobstoreInputStream(blobKey);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			CsvToBean<QuestionBean> csv = new CsvToBean<QuestionBean>();
			CSVReader reader = new CSVReader(in);
			String [] header = reader.readNext();

			ColumnPositionMappingStrategy<QuestionBean> strat = new ColumnPositionMappingStrategy<QuestionBean>();
			strat.setType(QuestionBean.class);
			strat.captureHeader(reader);
			strat.setColumnMapping(header);

			List<QuestionBean> list = csv.parse(strat, reader);


			Queue queueEntities = QueueFactory.getQueue("quizquestions");
			
			for (QuestionBean ce : list) {
				queueEntities.add(Builder.withUrl("/addQuizQuestion")
						.param("relation", relation)
						.param("mid", ce.getMid())
						.param("name", ce.getName())
						.param("weight", ce.getWeight().toString())
						.method(TaskOptions.Method.POST));
				
			}
			

			


		} catch (Exception e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
		}
	}

}
