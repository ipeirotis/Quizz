<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Question"%>
<%@ page import="java.util.List"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta name="google-site-verification" content="kYjnyRwCqe4JTpWbEjE-yL7ae3YPFf8zxlQuGcKGb-Q" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>List of Supported Relations</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet"
	media="screen">
<script src="http://code.jquery.com/jquery.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>



</head>
<body>
	<div class="container pagination-centered">
		<div class="row span12">
		<h2>Crowd Quizzes</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Relation</th>
					<th>Question text</th>
					<th>Freebase Attribute of Answer</th>
					<th>Freebase Type of Answer</th>
				</tr>
				<%
					PersistenceManager pm = PMF.get().getPersistenceManager();
					String query = "select from " + Question.class.getName();
					List<Question> questions = (List<Question>) pm.newQuery(query).execute();
					if (questions.isEmpty()) {
				%>
				<tr>
					<td colspan="4" style="text-align: center">No Questions found!</td>
				</tr>
				<%
					} else {
				%>


				<%
					for (Question q : questions) {
				%>
				<tr>
					<td><a
						href="/listEntities.jsp?relation=<%=q.getRelation()%>"><%=q.getRelation()%></a></td>
					<td><%=q.getQuestionText()%></td>
					<td><%=q.getFreebaseAttribute()%></td>
					<td><%=q.getFreebaseType()%></td>
				</tr>
				<%
					}
					}

					pm.close();
				%>
			</table>
			<div class="row span4 offset4">
				<a href="addRelation.jsp">Add a new relation to crowdsource</a>
			</div>
		</div>
	</div>
	
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