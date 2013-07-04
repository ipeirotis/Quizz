<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="java.util.List"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta name="google-site-verification" content="kYjnyRwCqe4JTpWbEjE-yL7ae3YPFf8zxlQuGcKGb-Q" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Quizz: Available Quizzes</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet"
	media="screen">
<script src="http://code.jquery.com/jquery.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>



</head>
<body>
	<div class="container pagination-centered">
		<div class="row span4 offset4">
		<h2>Available Quizzes</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Quiz</th>
				</tr>
				<%
				
			
				PersistenceManager pm = PMF.get().getPersistenceManager();
				String query = "select from " + Quiz.class.getName();
				List<Quiz> questions = (List<Quiz>) pm.newQuery(query).execute();
				pm.close();
				
				if (questions.isEmpty()) {
					%>
					<tr>
						<td style="text-align: center">No quizzes found!</td>
					</tr>
					<%
				} else {
					for (Quiz q : questions) {
						%>
						<tr>
						<td>
						<a href="/processUserAnswer?relation=<%=q.getRelation()%>"><%=q.getName()%></a>
						(<div style="display: inline" name="num_answered" quiz="<%=q.getRelation()%>">...</div>/<div style="display: inline" name="num_questions" quiz="<%=q.getRelation()%>">...</div>)
						</td>
						</tr>
						<%
					}
				}
				%>
			</table>
		</div>
	</div>
		<script type="text/javascript">
	<!-- For all table cells with the name FreebaseName, take the id of the cell, 	  -->
		$('div[name^="num_questions"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getNumberOfQuizQuestions';
			var params = {
				'quiz' : quiz,
			};
			$.getJSON(url, params)
			.done(function(response) {
				element.html(response.questions);
			});
		});
		
		$('div[name^="num_answered"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getNumberOfSubmittedAnswers';
			var params = {
				'quiz' : quiz,
				'userid' : '<%=Helper.getUseridFromCookie(request, response)%>'
			};
			$.getJSON(url, params)
			.done(function(response) {
				element.html(response.questions);
			});
		});
		</script>
	
	
	<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-89122-22', 'crowd-power.appspot.com');
  ga('send', 'pageview');

</script>
	
</body>
</html>