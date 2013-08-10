package com.ipeirotis.crowdquiz.servlets;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import us.quizz.repository.QuizRepository;

import com.ipeirotis.crowdquiz.entities.UserAnswer;
import com.ipeirotis.crowdquiz.utils.PMF;

@SuppressWarnings("serial")
public class DownloadUserAnswers extends HttpServlet {

	final static Logger					logger						= Logger.getLogger("com.ipeirotis.crowdquiz");

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String relation = request.getParameter("relation");
		String name = QuizRepository.getQuiz(relation).getName();

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(UserAnswer.class);
		query.setFilter("relation == relationParam");
		query.declareParameters("String relationParam");

		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) query.execute(relation);
		StringBuffer sb = new StringBuffer();
		sb.append("userid").append("\t")
			.append("mid").append("\t")
			.append("useranswer").append("\t")
			.append("action").append("\t")
			.append("ipaddress").append("\t")
			.append("timestamp").append("\t")
			.append("browser").append("\t")
			.append("referer").append("\n");
			
		for (UserAnswer ue : answers) {
			String userid = (ue.getUserid()==null)?"":ue.getUserid();
			String mid = (ue.getMid()==null)?"":ue.getMid();
			String useranswer = (ue.getUseranswer() ==null)?"":ue.getUseranswer(); 
			String action = (ue.getAction()==null)?"":ue.getAction();
			String ipaddress = (ue.getIpaddress()==null)?"":ue.getIpaddress();
			String timestamp = (ue.getTimestamp()==null)?"":ue.getTimestamp().toString();
			String browser = (ue.getBrowser()==null)?"":ue.getBrowser();
			String referer = (ue.getReferer()==null)?"":ue.getReferer();
			
			
			sb.append(userid).append("\t")
			.append(mid).append("\t")
			.append(useranswer).append("\t")
			.append(action).append("\t")
			.append(ipaddress).append("\t")
			.append(timestamp).append("\t")
			.append(browser).append("\t")
			.append(referer)
			.append("\n");
		}
		pm.close();
		

		response.setContentType("text/tab-separated-values; charset=UTF-8");
		response.addHeader("cache-control", "must-revalidate");
		response.addHeader("pragma", "must-revalidate");
		response.addHeader("content-disposition", "attachment; filename=\"" + name + "-answers.tsv\"");
		response.resetBuffer();
		response.setBufferSize(sb.length());
		response.getOutputStream().print(sb.toString());
		response.flushBuffer();
	}

}
