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
	for (UserEntry ue : answers) {
		sb.append(ue.getUserid() + "," + ue.getMid() + "," + ue.getUseranswer() + "\n");
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

