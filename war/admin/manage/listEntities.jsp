<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="java.util.List"%>
<%@ page import="javax.jdo.Query"%>
<%
	Quiz quiz = null;
	PersistenceManager pm = PMF.get().getPersistenceManager();
		String relation = request.getParameter("relation");
		String name = "";
		try {
	quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
	name = quiz.getName();
		} catch (Exception e) {
	
		}
%>

<jsp:include page="/header.jsp"><jsp:param name="title"
		value="Questions for quiz '<%=quiz.getName()%>' (<%=quiz.getRelation()%>)" /></jsp:include>


<body>
	<div class="container pagination-centered">
		<table class="table table-striped  table-bordered">

			<tr>
				<th colspan="2">Questions for quiz <%=name%></th>

			</tr>
			<%

				Query query = pm.newQuery(QuizQuestion.class);
				query.setFilter("relation == relationParam");
				query.declareParameters("String lastNameParam");
				query.setOrdering("weight DESC");
				List<QuizQuestion> questions = (List<QuizQuestion>) query.execute(quiz);
		
				if (questions.isEmpty()) {
			%>
			<tr>
				<td>No entries found!</td>
			</tr>
			<%
				} else {
								for (QuizQuestion q: questions) {
			%>
			<tr>
				<td><div name="FreebaseName" id="<%=q.getFreebaseEntityId()%>">Loading...</div></td>
				<td><a target="_blank" name="GoogleQuery"
					id="<%=q.getFreebaseEntityId()%>">Google</a></td>
				<td><a target="_blank"
					href="http://www.freebase.com<%=q.getFreebaseEntityId()%>">Freebase</a></td>
				<td><a
					href="/askQuestion.jsp?mid=<%=q.getFreebaseEntityId()%>&relation=<%=q.getRelation()%>">Fill
						the answer</a></td>
				<td><a
					href="/multChoice.jsp?mid=<%=q.getFreebaseEntityId()%>&relation=<%=q.getRelation()%>">Multiple
						choice</a></td>
			</tr>
			<%
				}
						}
						pm.close();
			%>
		</table>
	</div>

	<script type="text/javascript">
	<!-- For all table cells with the name FreebaseName, take the id of the cell, 	  -->
		$('div[name^="FreebaseName"]').each(function(index) {
			queryFreebase($(this).attr('id'), $(this));
		});
		
		$('a[name^="GoogleQuery"]').each(function(index) {
			queryFreebase2($(this).attr('id'), $(this), "http://www.google.com/search?q=<%=quiz.getQuestionText()%>
		");
						});

		function queryFreebase(freebaseMid, element) {

			var query = {
				'mid' : freebaseMid,
				'name' : null,
			};

			var params = {
				'key' : 'AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc',
				'limit' : 1,
				'exact' : true,
				'query' : JSON.stringify(query)
			};

			url = 'https://www.googleapis.com/freebase/v1/mqlread';

			$.getJSON(url + '?callback=?', params, function(response) {
				updateNames(response, element)
			});

		}

		function updateNames(response, element) {
			element.html(response.result.name);
		}

		function queryFreebase2(freebaseMid, element, prefix) {

			var query = {
				'mid' : freebaseMid,
				'name' : null,
			};

			var params = {
				'key' : 'AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc',
				'limit' : 1,
				'exact' : true,
				'query' : JSON.stringify(query)
			};

			url = 'https://www.googleapis.com/freebase/v1/mqlread';

			$.getJSON(url + '?callback=?', params, function(response) {
				updateNames2(response, element, prefix)
			});

		}

		function updateNames2(response, element, prefix) {
			element.attr("href", prefix + response.result.name);
		}
	</script>

</body>
</html>