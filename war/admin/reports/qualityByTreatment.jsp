<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Treatment"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.UserReferal"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.TreeSet"%>
<%@ page import="java.text.NumberFormat"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Conversion rate" /></jsp:include>
<% String relation = request.getParameter("quiz"); %>

<body>
	<div class="container pagination-centered">
		<div class="row">
			<h2>Contribution Quality by Treatment</h2>
				<%
				PersistenceManager	pm = PMF.get().getPersistenceManager();
				Query query;
				query = pm.newQuery(Treatment.class);
				List<Treatment> treatments = (List<Treatment>) query.execute();
				
				%>
			<table class="table table-striped  table-bordered" style="font-size:small">
				<tr>
					<th>User</th>
					<th>Quiz</th>
					<%
					for (Treatment t: treatments) {
						 %><th><%=t.getName() %></th><%
					}
					%>
					<th>#Correct</th>
					<th>#Total</th>
					<th>Quality</th>
					<th>Score</th>
				</tr>
				<%

					query = pm.newQuery(QuizPerformance.class);
					query.setOrdering("score desc");
					List<QuizPerformance> perf = null;
					if (relation!=null) {
						query.setFilter("quiz == quizParam");
						query.declareParameters("String quizParam");
						
						perf = (List<QuizPerformance>) query.execute(relation);
					} else {
						perf = (List<QuizPerformance>) query.execute();
					}
					
					
					for (QuizPerformance qp: perf) {
						User user = null;
						try {
							user = pm.getObjectById(User.class, User.generateKeyFromID(qp.getUserid()));
						} catch (Exception e) {
							
						}
						%>
						<tr>
							<td nowrap><%=user.getUserid()%></td>
							<td nowrap><%=qp.getQuiz()%></a></td>
							<%
							for (Treatment t : treatments) {
								%><td><%=user.getsTreatment(t.getName())%></td><%
							}
							%>
							<td><%=qp.getCorrectanswers()%></td>
							<td><%=qp.getTotalanswers()%></td>
							<td><%=qp.displayPercentageCorrect()%></td>
							<td><%=qp.displayScore()%></td>
						</tr>
						<%
					}
			
				//}
				pm.close();
				%>

			</table>
		</div>
	</div>
</body>
</html>