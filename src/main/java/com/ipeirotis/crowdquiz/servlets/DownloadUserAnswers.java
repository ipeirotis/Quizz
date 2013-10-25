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

		PersistenceManager pm = PMF.getPM();
		Query query = pm.newQuery(UserAnswer.class);
		query.setFilter("relation == relationParam");
		query.declareParameters("String relationParam");

		@SuppressWarnings("unchecked")
		List<UserAnswer> answers = (List<UserAnswer>) query.execute(relation);
		StringBuffer sb = new StringBuffer();
		sbApp(sb, "userid");
		sbApp(sb, "questionID");
		sbApp(sb, "useranswer");
		sbApp(sb, "action");
		sbApp(sb, "ipaddress");
		sbApp(sb, "timestamp");
		sbApp(sb, "browser");
		sb.append("referer").append("\n");
			
		for (UserAnswer ue : answers) {
			String userid = safeStr(ue.getUserid());
			String questionID = safeStr(ue.getQuestionID());
			String useranswer = safeStr(ue.getAnswerID());
			String action = safeStr(ue.getAction());
			String ipaddress = safeStr(ue.getIpaddress());
			String timestamp = safeStr(ue.getTimestamp());
			String browser = safeStr(ue.getBrowser());
			String referer = safeStr(ue.getReferer());
			
			sbApp(sb, userid);
			sbApp(sb, questionID);
			sbApp(sb, useranswer);
			sbApp(sb, action);
			sbApp(sb, ipaddress);
			sbApp(sb, timestamp);
			sbApp(sb, browser);
			sb.append(referer).append("\n");
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
	
	protected String safeStr(Object obj) {
		return (obj == null) ? "" : obj.toString();
	}
	
	protected void sbApp(StringBuffer sb, String text) {
		sb.append(text).append("\t");
	}

}
