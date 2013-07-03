<%@ page language="java" contentType="text/csv; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.UserAnswer"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.*"%>

<%
	PersistenceManager pm = PMF.get().getPersistenceManager();
	String relation = request.getParameter("relation");
	String name = "";
	try {
		Quiz q = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
		name = q.getName();
	} catch (Exception e) {

	}

	Query query = pm.newQuery(UserAnswer.class);
	query.setFilter("relation == relationParam");
	query.declareParameters("String relationParam");

	List<UserAnswer> answers = (List<UserAnswer>) query.execute(relation);
	StringBuffer sb = new StringBuffer();
	sb.append("userid").append(",")
		.append("mid").append(",")
		.append("useranswer").append(",")
		.append("action").append(",")
		.append("ipaddress").append(",")
		.append("timestamp").append(",")
		.append("browser").append(",")
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
		
		
		sb.append(userid).append(",")
		.append(mid).append(",")
		.append(useranswer).append(",")
		.append(action).append(",")
		.append(ipaddress).append(",")
		.append(timestamp).append(",")
		.append(browser).append(",")
		.append(referer)
		.append("\n");
	}
	pm.close();
	
	System.out.println(sb.toString());

	response.setContentType("text/csv; charset=UTF-8");
	response.addHeader("cache-control", "must-revalidate");
	response.addHeader("pragma", "must-revalidate");
	response.addHeader("content-disposition", "attachment; filename=\"" + name + "-answers.csv\"");
	response.resetBuffer();
	response.setBufferSize(sb.length());
	response.getOutputStream().print(sb.toString());
	response.flushBuffer();
%>

