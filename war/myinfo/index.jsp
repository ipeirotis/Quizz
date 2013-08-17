<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>

<%@ page import="us.quizz.repository.QuizPerformanceRepository"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="User info" /></jsp:include>

<body>
	<div class="container pagination-centered">
		<div class="row span8 offset2">
		<h2>User info</h2>
			<table class="table table-striped  table-bordered">
			
				<tr><th colspan="4">User Information</th></tr>
				<% User u = User.getUseridFromCookie(request, response); %>
				<tr>
					<td  class="span2">Userid:</td>
					<td class="span6" colspan="3"><%=u.getUserid() %></td>
				</tr>
			
				<tr><th colspan="4">Treatments</th></tr>
				<%  
				Map<String, Boolean> treatments = u.getTreatments();
				for (String name : treatments.keySet()) {
					%> 
					<tr>
						<td  class="span2"><%= name %></td>
						<td class="span6" colspan="3"><%= treatments.get(name) %></td>
					</tr>
					<%	
				}
				%>
			
				<tr><th colspan="4">Quiz Performance</th></tr>
				<%
				List<QuizPerformance> results = QuizPerformanceRepository.getQuizPerformancesByUser(u.getUserid());				
			
				for (QuizPerformance qp : results) {
					%> 
					<tr>
						<td  class="span2"><%= qp.getQuiz() %></td>
						<td  class="span2">Correct (%)<br><%= qp.getCorrectanswers() %>/<%= qp.getTotalanswers() %> (<%= qp.displayPercentageCorrect() %>)</td>
						<td  class="span2">Rank (%correct)<br><%= qp.getRankPercentCorrect() %>/<%=qp.getTotalUsers() %> (Top-<%= qp.displayRankPercentageCorrect() %>)</td>
						<td  class="span2">Rank (#correct)<br><%= qp.getRankTotalCorrect() %>/<%=qp.getTotalUsers() %> (Top-<%= qp.displayRankTotalCorrect() %>)</td>
		
					</tr>
					<%	
				}
				
				%>
			</table>
			

		</div>
	</div>

</body>
</html>