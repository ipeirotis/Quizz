<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="List of available quizzes" /></jsp:include>

<body>
	<div class="container pagination-centered">
		<div class="row span4 offset4">
		<h2>Available <span style="color: maroon">Quizz</span>es</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Quiz</th>
				</tr>
				<%
				PersistenceManager pm = PMF.get().getPersistenceManager();
				Query query = pm.newQuery(Quiz.class);
				@SuppressWarnings("unchecked")
				List<Quiz> questions = (List<Quiz>) query.execute();
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
						<a href="/startQuiz?relation=<%=q.getRelation()%>"><%=q.getName()%></a>
						<br><small>(Your progress: <div style="display: inline" name="num_answered" quiz="<%=q.getRelation()%>">...</div>/<div style="display: inline" name="num_questions" quiz="<%=q.getRelation()%>">...</div>)</small>
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
				'userid' : '<%=User.getUseridFromCookie(request, response).getUserid()%>'
			};
			$.getJSON(url, params)
			.done(function(response) {
				element.html(response.questions);
			});
		});
		</script>
	
<%@ include file="social-sharing.html" %>
<%@ include file="google-analytics.html" %>
	
</body>
</html>