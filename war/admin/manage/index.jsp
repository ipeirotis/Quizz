<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="java.util.List"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Manage available quizzes" /></jsp:include>

<body>
	<div class="container pagination-centered">
		<div class="row span12">
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
					PersistenceManager pm = PMF.get().getPersistenceManager();
					String query = "select from " + Quiz.class.getName();
					List<Quiz> questions = (List<Quiz>) pm.newQuery(query).execute();
					if (questions.isEmpty()) {
						;
					} else {
						for (Quiz q : questions) {
							%>
							<tr>
								<td><%=q.getRelation()%></td>
								<td><%=q.getName()%><br>
									<a 
									href="listEntities.jsp?relation=<%=q.getRelation()%>">List</a>|<a 
									href="edit_quiz.jsp?relation=<%=q.getRelation()%>">Edit</a>|<a 
									href="/api/deleteQuiz?relation=<%=q.getRelation()%>">Delete</a>
								</td>
								<td><%=q.getQuestionText()%></td>
								<td><a id="upload_questions" title="Upload additional entities for which we want to ask the quiz question" href="upload_questions.jsp?relation=<%=q.getRelation()%>">Questions</a>&nbsp;(<div style="display: inline" name="num_questions" quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="upload_gold"	title="Upload correct answers for the questions asked" href="upload_gold.jsp?relation=<%=q.getRelation()%>">Gold</a>&nbsp;(<div  style="display: inline"  name="num_gold"  quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="upload_silver" title="Upload possible answers for the questions asked (with some <1 probability of being correct)" href="upload_silver.jsp?relation=<%=q.getRelation()%>">Silver</a>&nbsp;(<div   style="display: inline"  name="num_silver" quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="upload_crowd" title="Upload answers submitted by users (that have been downloaded previously from Quizz.us)" href="upload_crowd.jsp?relation=<%=q.getRelation()%>">Crowd</a>&nbsp;(<div  style="display: inline"  name="num_answers" quiz="<%=q.getRelation()%>">...</div>)</td>
								<td><a id="download" title="Download the answers submitted so far by the users" href="downloadUserAnswers?relation=<%=q.getRelation()%>">Answers</a>&nbsp;(<div  style="display: inline"  name="num_answers" quiz="<%=q.getRelation()%>">...</div>)</td>
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
			<small><a href="/api/updateCountStatistics">Update counters</a></small>
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
				element.html(response.questions);
			});
		});
		
		
		
	</script>

</body>
</html>