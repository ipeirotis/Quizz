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
					<th>Question text</th>
					<th style="text-align: center" colspan="4">Upload</th>
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
									href="edit_quiz.jsp?relation=<%=q.getQuizID()%>">Edit</a>|<a 
									href="/api/deleteQuiz?relation=<%=q.getQuizID()%>">Delete</a>
								</td>
								<td><%=q.getQuestionText()%></td>
								<td><a id="upload_questions" title="Upload additional entities for which we want to ask the quiz question" href="upload_questions.jsp?relation=<%=q.getQuizID()%>">Questions</a>&nbsp;(<div style="display: inline" name="num_questions" quiz="<%=q.getQuizID()%>">...</div>)</td>
								<td><a id="upload_gold"	title="Upload correct answers for the questions asked" href="upload_gold.jsp?relation=<%=q.getQuizID()%>">Gold</a>&nbsp;(<div  style="display: inline"  name="num_gold"  quiz="<%=q.getQuizID()%>">...</div>)</td>
								<td><a id="upload_silver" title="Upload possible answers for the questions asked (with some <1 probability of being correct)" href="upload_silver.jsp?relation=<%=q.getQuizID()%>">Silver</a>&nbsp;(<div   style="display: inline"  name="num_silver" quiz="<%=q.getQuizID()%>">...</div>)</td>
								<td><a id="upload_crowd" title="Upload answers submitted by users (that have been downloaded previously from Quizz.us)" href="upload_crowd.jsp?relation=<%=q.getQuizID()%>">Answers</a>&nbsp;(<div  style="display: inline"  name="num_answers" quiz="<%=q.getQuizID()%>">...</div>)</td>
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
		
		$('div[name^="num_silver"]').each(function(index) {
			var element = $(this);
			var quiz = element.attr('quiz');
			var url = '/api/getQuizCounts';
			var params = {
				'quiz' : quiz,
			};
			$.getJSON(url, params)
			.done(function(response) {
				element.html(response.silver);
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