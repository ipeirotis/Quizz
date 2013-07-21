<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Treatment"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.UserReferal"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.TreeSet"%>
<%@ page import="java.text.NumberFormat"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Conversion rate" /></jsp:include>

<body>
	<div class="container pagination-centered">
		<div class="row span10 offset1">
			<h2>User Conversion Rate</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Quiz</th>
					<th>Users</th>
					<th>Users with contributions</th>
					<th>Conversion rate</th>
				</tr>
				<%
				PersistenceManager	pm = PMF.get().getPersistenceManager();
				Query query = pm.newQuery(Quiz.class);
				@SuppressWarnings("unchecked")
				List<Quiz> quizzes = (List<Quiz>) query.execute();
				for (Quiz quiz : quizzes) {
					query = pm.newQuery(UserReferal.class);
					query.setFilter("quiz == quizParam");
					query.declareParameters("String quizParam");
					List<UserReferal> referals = (List<UserReferal>) query.execute(quiz.getRelation());
					TreeSet<String> userids = new TreeSet<String>();
					for (UserReferal ur : referals) {
						userids.add(ur.getUserid());
					}
					
					query = pm.newQuery(QuizPerformance.class);
					query.setFilter("quiz == quizParam");
					query.declareParameters("String quizParam");
					List<QuizPerformance> qp = (List<QuizPerformance>) query.execute(quiz.getRelation());

					NumberFormat percentFormat = NumberFormat.getPercentInstance();
					percentFormat.setMaximumFractionDigits(0);
					String rate = percentFormat.format(1.0*(qp.size()+1)/(userids.size()+1));
					
					%>
					<tr>
						<td><%=quiz.getName()%></td>
						<td><%=userids.size()+1%></td>
						<td><%=qp.size()+1%></td>
						<td><%=rate%></td>
					</tr>
					<%
				}
				pm.close();
				%>

			</table>
		</div>
	</div>
</body>
</html>