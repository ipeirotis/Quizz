<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Test yourself, Compare yourself, Learn new things" /></jsp:include>

<body>
	<div class="container" style="text-align: center; max-width:640px">

		<h2><span style="color: maroon">Quizz</span>es</h2>
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
						<br><small>(Your progress: <span style="display: inline" name="num_answered" quiz="<%=q.getRelation()%>">...</span>/<span style="display: inline" name="num_questions" quiz="<%=q.getRelation()%>">...</span>)</small>
						</td>
						</tr>
						<%
					}
				}
				%>
			</table>

<%@ include file="assets/social-sharing.html" %>		
		
	</div>

	<script type="text/javascript">
	<!-- For all table cells with the name FreebaseName, take the id of the cell, 	  -->
		$('span[name^="num_questions"]').each(function(index) {
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
		
		$('span[name^="num_answered"]').each(function(index) {
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
	

<%@ include file="assets/google-analytics.html" %>
	
</body>
</html>