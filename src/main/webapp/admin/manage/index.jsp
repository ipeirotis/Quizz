<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="java.util.List"%>
<%@ page import="us.quizz.repository.QuizRepository"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Manage available quizzes" /></jsp:include>

<body>
	<div class="container">
		<div class="well">
			<h2>Available <span style="color: maroon">Quizz</span>es</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>ID</th>
					<th>Name</th>
					<th>Download</th>
				</tr>

				<%
					List<Quiz> quizzes = QuizRepository.getQuizzes();
					if (quizzes.isEmpty()) {
						;
					} else {
						for (Quiz q : quizzes) {
							%>
							<tr>
								<td><%=q.getQuizID()%></td>
								<td><%=q.getName()%><br>
									<a 
									href="listEntities.jsp?relation=<%=q.getQuizID()%>">List</a>|<a 
									href="/api/deleteQuiz?relation=<%=q.getQuizID()%>">Delete</a>
								</td>
								<td><a id="download" title="Download the answers submitted so far by the users" href="/admin/downloadUserAnswers?relation=<%=q.getQuizID()%>">Answers</a></td>
							</tr>
							<%
						}
					}
					
				%>
				<tr>
					<td colspan="10" style="text-align: center">
						<a href="create_quiz.jsp">Create a new quiz</a>
					</td>
				</tr>
			</table>
			<small><a href="/api/updateCountStatistics">Update counters</a></small>
		</div>
	</div>

	<script type="text/javascript">

		$('div[name^="num_questions"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getQuizCounts';
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
			var url = '/api/getQuizCounts';
			var params = {
				'quiz' : quiz,
			};
			$.getJSON(url, params)
			.done(function(response) {
				element.html(response.gold);
			});
		});
		
		
		$('div[name^="num_answers"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getQuizCounts';
			var params = {
				'quiz' : quiz,
			};
			$.getJSON(url, params)
			.done(function(response) {
				element.html(response.submitted);
			});
		});
		
		
		
	</script>

</body>
</html>