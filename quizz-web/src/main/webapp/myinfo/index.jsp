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

<%@ page import="us.quizz.service.QuizPerformanceService"%>
<%@ page import="com.google.inject.Injector"%>
<%@ page import="com.google.inject.Guice"%>
<%@ page import="us.quizz.di.CommonModule"%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Quizz.us: myinfo</title>
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.css">

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js" type="text/javascript"></script>
<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.js" type="text/javascript"></script>

<link rel="shortcut icon" type="image/x-icon" href="/assets/favicon.ico">

<link rel="apple-touch-icon-precomposed" sizes="144x144" href="/assets/144x144.png">
<link rel="apple-touch-icon-precomposed" sizes="114x114" href="/assets/114x114.png">
<link rel="apple-touch-icon-precomposed" sizes="72x72" href="/assets/72x72.png">
<link rel="apple-touch-icon-precomposed" href="/assets/57x57.png">

</head>
<% 
Injector i = Guice.createInjector(new CommonModule());
UserRepository userRepository = i.getInstance(UserRepository.class);
QuizPerformanceService quizPerformanceService = i.getInstance(QuizPerformanceService.class);
%>

<body>
	<div class="container">
		<div class="row">
			<div class="col-sm-offset-2 col-sm-8 col-md-offset-2 col-md-8 col-lg-offset-2 col-lg-8">
			<h2>User info</h2>
				<table class="table table-striped table-bordered">
				
					<tr><th colspan="4">User Information</th></tr>
					<% User u = userRepository.getUseridFromCookie(request, response); %>
					<tr>
						<td>Userid:</td>
						<td colspan="3"><%=u.getUserid() %></td>
					</tr>
				
					<tr><th colspan="4">Treatments</th></tr>
					<%  
					Map<String, Boolean> treatments = u.getTreatments();
					for (String name : treatments.keySet()) {
						%> 
						<tr>
							<td><%= name %></td>
							<td colspan="3"><%= treatments.get(name) %></td>
						</tr>
						<%	
					}
					%>
				
					<tr><th colspan="4">Quiz Performance</th></tr>
					<%
					List<QuizPerformance> results = quizPerformanceService.getQuizPerformancesByUser(u.getUserid());				
				
					for (QuizPerformance qp : results) {
						%> 
						<tr>
							<td><%= qp.getQuiz() %></td>
							<td>Correct (%)<br><%= qp.getCorrectanswers() %>/<%= qp.getTotalanswers() %> (<%= qp.displayPercentageCorrect() %>)</td>
							<td>Rank<br><%= qp.getRankScore() %>/<%=qp.getTotalUsers() %> (Top-<%= qp.displayRankScore() %>)</td>
							
						</tr>
						<%	
					}
					
					%>
				</table>
	
			</div>
		</div>
	</div>
</body>
</html>