<%@ page language="java" contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="us.quizz.entities.Quiz"%>
<%@ page import="java.util.List"%>
<%@ page import="java.text.NumberFormat"%>
<%@ page import="com.google.inject.Injector"%>
<%@ page import="com.google.inject.Guice"%>
<%@ page import="us.quizz.repository.QuizRepository"%>
<%@ page import="us.quizz.di.CommonModule"%>

<jsp:include page="/admin/header.jsp"><jsp:param name="title" value="Conversion rate" /></jsp:include>

<body>
	<div class="container">
		<div class="well">
			<h2>Contribution Quality</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Quiz</th>
					<th>Total Users</th>
					<th>Contributing Users</th>
					<th>Conversion Rate</th>
					<th>#All Answers</th>
					<th>#NonIDK Answers</th>
					<th>#Correct Answers</th>
					<th>Avg. User Quality</th>
					<th>Avg. Answer Quality</th>
					<th>Bits/User</th>
					<th>Capacity @ 99%</th>
					<th>Capacity @ 95%</th>
					<th>Capacity @ 90%</th>
				</tr>
				<%
				NumberFormat percentFormat = NumberFormat.getPercentInstance();
				percentFormat.setMaximumFractionDigits(0);
				
				NumberFormat format = NumberFormat.getInstance();
				format.setMinimumFractionDigits(2);
				format.setMaximumFractionDigits(2);

				Injector i = Guice.createInjector(new CommonModule());
				QuizRepository quizRepository = i.getInstance(QuizRepository.class);
				List<Quiz> quizzes = quizRepository.getQuizzes();
				for (Quiz quiz : quizzes) {
					
					int totalUsers = quiz.getContributingUsers();
					
					double capacity99 = quiz.getCapacity(0.01);
					double capacity95 = quiz.getCapacity(0.05);
					double capacity90 = quiz.getCapacity(0.10);
					%>
					<tr>
						<td><a href="qualityByTreatment.jsp?quiz=<%=quiz.getQuizID() %>&start=0&end=500"><%=quiz.getName()%></a></td>
						<td><%=quiz.getTotalUsers()%></td>
						<td><%=quiz.getContributingUsers()%></td>
						<td><%=percentFormat.format(quiz.getConversionRate())%></td>
						<td><%=quiz.getSubmitted()%></td>
						<td><%=quiz.getTotalAnswers()%></td>
						<td><%=quiz.getCorrectAnswers()%></td>
						<td><%=percentFormat.format(quiz.getAvgUserCorrectness())%></td>
						<td><%=percentFormat.format(quiz.getAvgAnswerCorrectness())%></td>
						<td><%=format.format(quiz.getCapacity()) %></td>
						<td><%=format.format(capacity99*totalUsers) %></td>
						<td><%=format.format(capacity95*totalUsers) %></td>
						<td><%=format.format(capacity90*totalUsers) %></td>
					</tr>
					<%
				}
				
				%>

			</table>
		</div>
	</div>
</body>
</html>