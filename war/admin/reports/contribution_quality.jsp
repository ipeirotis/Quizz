<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Treatment"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.UserReferal"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.TreeSet"%>
<%@ page import="java.text.NumberFormat"%>
<%@ page import="us.quizz.repository.QuizRepository"%>
<%@ page import="us.quizz.repository.QuizPerformanceRepository"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Conversion rate" /></jsp:include>

<body>
	<div class="container">
		<div class="well">
			<h2>Contribution Quality</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Quiz</th>
					<th>Contributing Users</th>
					<th>#Correct Answers</th>
					<th>#Total Answers</th>
					<th>Avg. User Quality</th>
					<th>Capacity @ 99%</th>
					<th>Capacity @ 95%</th>
					<th>Capacity @ 90%</th>
				</tr>
				<%
				

				List<Quiz> quizzes = QuizRepository.getQuizzes();
				for (Quiz quiz : quizzes) {
					
					List<QuizPerformance> perf = QuizPerformanceRepository.getQuizPerformancesByQuiz(quiz.getRelation());
					
					int totalUsers = perf.size();
					int totalCorrect = 0;
					int totalAnswers = 0;
					double bits = 0;
					double avgCorrectness = 0;

					for (QuizPerformance qp: perf) {
						totalCorrect += qp.getCorrectanswers();
						totalAnswers += qp.getTotalanswers();
						avgCorrectness += qp.getPercentageCorrect();
						bits += qp.getScore();
						
					}
					
					NumberFormat percentFormat = NumberFormat.getPercentInstance();
					percentFormat.setMaximumFractionDigits(0);
					String avgUserCorrectness = percentFormat.format(avgCorrectness/totalUsers);
					
					NumberFormat format = NumberFormat.getInstance();
					format.setMinimumFractionDigits(1);
					format.setMaximumFractionDigits(1);
					
					double capacity100 = (bits/2)/totalUsers;
					quiz.setCapacity(capacity100);
					
					double capacity99 = quiz.getCapacity(0.01);
					double capacity95 = quiz.getCapacity(0.05);
					double capacity90 = quiz.getCapacity(0.10);

					quiz.setContributingUsers(totalUsers);
					quiz.setCorrectAnswers(totalCorrect);
					quiz.setTotalAnswers(totalAnswers);
					quiz.setAvgUserCorrectness(avgCorrectness/totalUsers);
					
					QuizRepository.storeQuiz(quiz);
					%>
					<tr>
						<td><a href="qualityByTreatment.jsp?quiz=<%=quiz.getRelation() %>"><%=quiz.getName()%></a></td>
						<td><%=quiz.getContributingUsers()%></td>
						<td><%=quiz.getCorrectAnswers()%></td>
						<td><%=quiz.getTotalAnswers()%></td>
						<td><%=percentFormat.format(quiz.getAvgUserCorrectness())%></td>
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