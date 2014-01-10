<%@page import="us.quizz.repository.QuizRepository"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Treatment"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.UserReferal"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.text.NumberFormat"%>

<%@ page import="us.quizz.repository.QuizRepository"%>
<%@ page import="us.quizz.repository.QuizPerformanceRepository"%>
<%@ page import="us.quizz.repository.UserReferralRepository"%>

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
				
				List<Quiz> quizzes = QuizRepository.getQuizzes();
				
				for (Quiz quiz : quizzes) {
					
					NumberFormat percentFormat = NumberFormat.getPercentInstance();
					percentFormat.setMaximumFractionDigits(0);
					
					%>
					<tr>
						<td><%=quiz.getName()%></td>
						<td><%=quiz.getTotalUsers()%></td>
						<td><%=quiz.getContributingUsers()%></td>
						<td><%=percentFormat.format(quiz.getConversionRate())%></td>
					</tr>
					<%
				}
				
				%>

			</table>
		</div>
	</div>
</body>
</html>