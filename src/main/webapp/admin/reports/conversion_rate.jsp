<%@ page language="java" contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="us.quizz.utils.PMF"%>
<%@ page import="us.quizz.entities.Quiz"%>
<%@ page import="us.quizz.entities.Treatment"%>
<%@ page import="us.quizz.entities.UserReferal"%>
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