<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="us.quizz.entities.Quiz"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.inject.Injector"%>
<%@ page import="com.google.inject.Guice"%>
<%@ page import="us.quizz.service.QuizService"%>
<%@ page import="us.quizz.di.CommonModule"%>

<jsp:include page="../header.jsp"><jsp:param name="title" value="Manage available quizzes" /></jsp:include>

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
					Injector i = Guice.createInjector(new CommonModule());
					QuizService quizService = i.getInstance(QuizService.class);
					List<Quiz> quizzes = quizService.listAll();
					if (quizzes.isEmpty()) {
						;
					} else {
						for (Quiz q : quizzes) {
							%>
							<tr>
								<td><%=q.getQuizID()%></td>
								<td><%=q.getName()%></td>
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