<%@ page language="java" contentType="text/csv; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Question"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.UserEntry"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.EntityQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.*"%>

<%
	PersistenceManager pm = PMF.get().getPersistenceManager();
	String relation = request.getParameter("relation");
	String name = "";
	try {
		Question q = pm.getObjectById(Question.class, Question.generateKeyFromID(relation));
		name = q.getName();
	} catch (Exception e) {

	}

	Query query = pm.newQuery(UserEntry.class);
	query.setFilter("relation == relationParam");
	query.declareParameters("String relationParam");

	List<UserEntry> answers = (List<UserEntry>) query.execute(relation);
	StringBuffer sb = new StringBuffer();
	sb.append("userid").append(",")
		.append("mid").append(",")
		.append("useranswer").append(",")
		.append("action").append(",")
		.append("ipaddress").append(",")
		.append("timestamp").append(",")
		//.append("browser").append(",")
		.append("freebaseanswer").append("\n");
		
	for (UserEntry ue : answers) {
		String userid = (ue.getUserid()==null)?"":ue.getUserid();
		String mid = (ue.getMid()==null)?"":ue.getMid();
		String useranswer = (ue.getUseranswer() ==null)?"":ue.getUseranswer(); 
		String action = (ue.getAction()==null)?"":ue.getAction();
		String ipaddress = (ue.getIpaddress()==null)?"":ue.getIpaddress();
		String timestamp = (ue.getTimestamp()==null)?"":ue.getTimestamp().toString();
		String browser = (ue.getBrowser()==null)?"":ue.getBrowser();
		String freebaseanswer = (ue.getFreebaseanswer()==null)?"":ue.getFreebaseanswer();
		
		sb.append(userid).append(",")
		.append(mid).append(",")
		.append(useranswer).append(",")
		.append(action).append(",")
		.append(ipaddress).append(",")
		.append(timestamp).append(",")
		//.append(browser).append(",")
		.append(freebaseanswer).append("\n");
		
		//sb.append(userid + "\t" + ue.getMid() + "\t" + ue.getUseranswer() + "\t" + ipaddress  + "," + action + "\n");
	}
	pm.close();

	response.setContentType("text/csv; charset=UTF-8");
	response.addHeader("cache-control", "must-revalidate");
	response.addHeader("pragma", "must-revalidate");
	response.addHeader("content-disposition", "attachment; filename=\"" + name + "-answers.csv\"");
	response.resetBuffer();
	response.setBufferSize(sb.length());
	response.getOutputStream().print(sb.toString());
	response.flushBuffer();
%>

