<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<jsp:include page="/header.jsp"><jsp:param name="title" value="Test yourself, Compare yourself, Learn new things" /></jsp:include>

<body>
	<div class="container" style="text-align: center; max-width: 640px">

		<h2>
			<span style="color: maroon">Quizz</span>es
		</h2>
		<table class="table table-striped  table-bordered" id="quizzes">
			<tbody>
				<tr>
					<th>Quiz</th>
				</tr>
			</tbody>

		</table>
	</div>

	<script type="text/javascript">
	
	$(document).ready(function() {
		var user = getUsername();
		$.when(getQuizzes(), getUserQuizPerformances(user)).done(function(a1, a2){
			
			// The a1, a2 contain the return values from the getJSON as an array with three elements
			// The [0] element contains the data 
			quizItems = a1[0];
			quizperformanceItems = a2[0];
			
			var table = $("#quizzes tr:first");
			$.each(quizItems.items, function(key, quiz) {
				var row = $('<tr />');
				var cell = $('<td />');
				cell.append($('<a href="/startQuiz?relation=' + quiz.relation + '">' + quiz.name + '</a>'));
				cell.append($('<br><small>(Your progress: <span id="' + quiz.relation + '">0</span>/' + quiz.questions + ')</small>'));
				row.append(cell);
				table.after(row);
			});
			
			$.each(quizperformanceItems.items, function(key, quizperf) {
				var element = $(document.getElementById(quizperf.quiz));
				element.text(quizperf.totalanswers);
			});
		});
	});
	</script>

	<%@ include file="assets/google-analytics.html"%>

</body>
</html>