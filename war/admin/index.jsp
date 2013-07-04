<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="java.util.List"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta name="google-site-verification" content="kYjnyRwCqe4JTpWbEjE-yL7ae3YPFf8zxlQuGcKGb-Q" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>List of available quizzes</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="/bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
<script src="http://code.jquery.com/jquery.js"></script>
<script src="http://code.jquery.com/jquery-migrate-1.2.1.js"></script>
<script src="/bootstrap/js/bootstrap.min.js"></script>

<link type="text/css" rel="stylesheet" href="/qtip/jquery.qtip.css" />
<script type="text/javascript" src="/qtip/jquery.qtip.js"></script>

</head>
<body>
	<div class="container pagination-centered">
		<div class="row span12">
			<h2>Available <span style="color: maroon">Quizz</span>es</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Name</th>
					<th>ID</th>
					<th>Question text</th>
					<th>Answer Type</th>
					<th style="text-align: center" colspan="4">Upload</th>
					<th>Download</th>
					<th>Adcampaign</th>
				</tr>

				<%
					PersistenceManager pm = PMF.get().getPersistenceManager();
					String query = "select from " + Quiz.class.getName();
					List<Quiz> questions = (List<Quiz>) pm.newQuery(query).execute();
					if (questions.isEmpty()) {
						;
					} else {
						for (Quiz q : questions) {
							%>
							<tr>
								<td><a href="/listEntities.jsp?relation=<%=q.getRelation()%>"><%=q.getName()%></a></td>
								<td><%=q.getRelation()%></td>
								<td><%=q.getQuestionText()%></td>
								<td><%=q.getFreebaseType()%></td>
								<td><a id="upload_questions" href="upload_questions.jsp?relation=<%=q.getRelation()%>">Questions</a>&nbsp;(<div style="display: inline" name="num_questions" quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="upload_gold"	href="upload_gold.jsp?relation=<%=q.getRelation()%>">Gold</a>&nbsp;(<div  style="display: inline"  name="num_gold"  quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="upload_silver" href="upload_silver.jsp?relation=<%=q.getRelation()%>">Silver</a>&nbsp;(<div   style="display: inline"  name="num_silver" quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="upload_crowd" href="upload_crowd.jsp?relation=<%=q.getRelation()%>">Crowd</a>&nbsp;(<div  style="display: inline"  name="num_answers" quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="download" href="downloadUserAnswers?relation=<%=q.getRelation()%>">Answers</a>&nbsp;(<div  style="display: inline"  name="num_answers" quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="adcampaign" href="manage_adcampaign.jsp?relation=<%=q.getRelation()%>">Manage</a></td>
							</tr>
							<%
						}
					}
					pm.close();
				%>
				<tr>
					<td colspan="10" style="text-align: center">
						<a href="create_quiz.jsp">Create a new quiz</a>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<!-- Setup help tooltips for the different page elements -->
	<script>
		$('#upload_gold').qtip({content : 'Upload correct answers for the questions asked'});
		$('#upload_silver').qtip({content : 'Upload possible answers for the questions asked (with some <1 probability of being correct)'});
		$('#upload_crowd').qtip({content : 'Upload answers submitted by users'});
		$('#upload_questions').qtip({content : 'Upload additional entities for which we want to ask the quiz question'});
		$('#download').qtip({content : 'Download the answers submitted so far by the users'});
		$('#adcampaign').qtip({content : 'Manage the ad campaign'});
	</script>

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
		
		$('div[name^="num_gold"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getNumberOfGoldAnswers';
			var params = {
				'quiz' : quiz,
			};
			$.getJSON(url, params)
			.done(function(response) {
				element.html(response.questions);
			});
		});
		
		$('div[name^="num_silver"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getNumberOfSilverAnswers';
			var params = {
				'quiz' : quiz,
			};
			$.getJSON(url, params)
			.done(function(response) {
				console.log("PANOS");
				console.log(response.questions);
				element.html(response.questions);
			});
		});
		
		
		$('div[name^="num_answers"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getNumberOfUserAnswers';
			var params = {
				'quiz' : quiz,
			};
			$.getJSON(url, params)
			.done(function(response) {
				console.log("PANOS");
				console.log(response.questions);
				element.html(response.questions);
			});
		});
		
		
		
	</script>

	<script>
		(function(i, s, o, g, r, a, m) {
			i['GoogleAnalyticsObject'] = r;
			i[r] = i[r] || function() {
				(i[r].q = i[r].q || []).push(arguments)
			}, i[r].l = 1 * new Date();
			a = s.createElement(o), m = s.getElementsByTagName(o)[0];
			a.async = 1;
			a.src = g;
			m.parentNode.insertBefore(a, m)
		})(window, document, 'script',
				'//www.google-analytics.com/analytics.js', 'ga');
		ga('create', 'UA-89122-22', 'crowd-power.appspot.com');
		ga('send', 'pageview');
	</script>

</body>
</html>