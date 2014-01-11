package us.quizz.servlets;

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

import us.quizz.utils.Helper;
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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@SuppressWarnings("serial")
public class UploadCrowdAnswers extends HttpServlet {

	final static Logger logger = Logger.getLogger("com.ipeirotis.adcrowdkg");

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String baseURL = Helper.getBaseURL(req);
		String url = baseURL + "/admin/";
		resp.sendRedirect(url);

		try {
			String quizID = req.getParameter("quizID");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(quizID),
					"Empty quizID");

			BlobstoreService blobstoreService = BlobstoreServiceFactory
					.getBlobstoreService();
			@SuppressWarnings("deprecation")
			Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
			BlobKey blobKey = blobs.get("answers_file");

			BlobstoreInputStream is = new BlobstoreInputStream(blobKey);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			CsvToBean<UserAnswerBean> csv = new CsvToBean<UserAnswerBean>();
			CSVReader reader = new CSVReader(in, '\t');
			String[] header = reader.readNext();

			ColumnPositionMappingStrategy<UserAnswerBean> strat = new ColumnPositionMappingStrategy<UserAnswerBean>();
			strat.setType(UserAnswerBean.class);
			strat.captureHeader(reader);
			strat.setColumnMapping(header);

			List<UserAnswerBean> list = csv.parse(strat, reader);

			Queue queue = QueueFactory.getQueue("answers");

			for (UserAnswerBean ce : list) {

				queue.add(Builder.withUrl("/addUserAnswer")
						.param("quizID", quizID)
						.param("userid", ce.getUserid())
						.param("action", ce.getAction())
						.param("questionID", ce.getQuestionID())
						.param("useranswerID", "" + ce.getUseranswerID())
						.param("browser", ce.getBrowser())
						.param("ipAddress", ce.getIpaddress())
						.param("referer", ce.getReferer())
						.param("timestamp", ce.getTimestamp().toString())
						.method(TaskOptions.Method.POST));
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"Reached execution time limit. Press refresh to continue.",
					e);
		}
	}

}
