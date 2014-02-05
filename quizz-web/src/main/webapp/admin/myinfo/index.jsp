<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="us.quizz.utils.PMF"%>
<%@ page import="us.quizz.entities.QuizPerformance"%>
<%@ page import="us.quizz.entities.User"%>
<%@ page import="us.quizz.repository.UserRepository"%>
<%@ page import="us.quizz.utils.Helper"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>

<%@ page import="us.quizz.repository.QuizPerformanceRepository"%>
<%@ page import="com.google.inject.Injector"%>
<%@ page import="com.google.inject.Guice"%>
<%@ page import="us.quizz.di.CommonModule"%>

<jsp:include page="/admin/header.jsp"><jsp:param name="title" value="User info" /></jsp:include>
<% 
Injector i = Guice.createInjector(new CommonModule());
UserRepository userRepository = i.getInstance(UserRepository.class);
QuizPerformanceRepository quizPerformanceRepository = i.getInstance(QuizPerformanceRepository.class);
%>

<body>
	<div class="container pagination-centered">
		<div class="row span8 offset2">
		<h2>User info</h2>
			<table class="table table-striped  table-bordered">
			
				<tr><th colspan="4">User Information</th></tr>
				<% User u = userRepository.getUseridFromCookie(request, response); %>
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
				List<QuizPerformance> results = quizPerformanceRepository.getQuizPerformancesByUser(u.getUserid());				
			
				for (QuizPerformance qp : results) {
					%> 
					<tr>
						<td  class="span2"><%= qp.getQuiz() %></td>
						<td  class="span2">Correct (%)<br><%= qp.getCorrectanswers() %>/<%= qp.getTotalanswers() %> (<%= qp.displayPercentageCorrect() %>)</td>
						<td  class="span2">Rank<br><%= qp.getRankScore() %>/<%=qp.getTotalUsers() %> (Top-<%= qp.displayRankScore() %>)</td>
						
					</tr>
					<%	
				}
				
				%>
			</table>
			

		</div>
	</div>

</body>
</html>