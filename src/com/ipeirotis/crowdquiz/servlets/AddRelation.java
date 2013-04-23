package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

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

import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class AddRelation extends HttpServlet {

	private HttpServletResponse	r;
  private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  
	final static Logger logger = Logger.getLogger("com.ipeirotis.adcrowdkg"); 
	
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
			
			Queue queue = QueueFactory.getDefaultQueue();
			BlobstoreInputStream is = new BlobstoreInputStream(blobKey);
			Scanner scanner = new Scanner(is);
			while(scanner.hasNextLine()){
				String s = scanner.nextLine();
				String[] entries = s.split(",");
				String mid = entries[0];
				String weight = entries[5];
				if (mid.equals("mid")) continue; // skip first line
				queue.add(Builder.withUrl("/addEntity").param("relation", relation).param("freebaseid", mid).param("emptyweight", weight).method(TaskOptions.Method.GET));
			}
			scanner.close();


		} catch (com.google.apphosting.api.DeadlineExceededException e) {
			logger.log(Level.SEVERE, "Reached execution time limit. Press refresh to continue.", e);
			
		}
	}
	

	
}
