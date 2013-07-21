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
			<h2>Contribution Quality</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Quiz</th>
					<th>Users</th>
					<th>#Correct Answers</th>
					<th>#Total Answers</th>
					<th>Avg. User Quality</th>
					<th>Avg. Answer Quality</th>
				</tr>
				<%
				PersistenceManager	pm = PMF.get().getPersistenceManager();
				Query query = pm.newQuery(Quiz.class);
				@SuppressWarnings("unchecked")
				List<Quiz> quizzes = (List<Quiz>) query.execute();
				for (Quiz quiz : quizzes) {
					query = pm.newQuery(QuizPerformance.class);
					query.setFilter("quiz == quizParam");
					query.declareParameters("String quizParam");
					List<QuizPerformance> perf = (List<QuizPerformance>) query.execute(quiz.getRelation());
					
					int totalUsers = perf.size();
					int totalCorrect = 0;
					int totalAnswers = 0;
					double avgCorrectness = 0;
					for (QuizPerformance qp: perf) {
						totalCorrect += qp.getCorrectanswers();
						totalAnswers += qp.getTotalanswers();
						avgCorrectness += qp.getPercentageCorrect();
					}
					
					NumberFormat percentFormat = NumberFormat.getPercentInstance();
					percentFormat.setMaximumFractionDigits(0);
					String avgUserCorrectness = percentFormat.format(avgCorrectness/totalUsers);
					String avgAnswerCorrectness = percentFormat.format(1.0*totalCorrect/totalAnswers);
					
					%>
					<tr>
						<td><a href="qualityByTreatment.jsp?quiz=<%=quiz.getRelation() %>"><%=quiz.getName()%></a></td>
						<td><%=totalUsers%></td>
						<td><%=totalCorrect%></td>
						<td><%=totalAnswers%></td>
						<td><%=avgUserCorrectness%></td>
						<td><%=avgAnswerCorrectness%></td>
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