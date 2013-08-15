<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.ipeirotis.crowdquiz.utils.*"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>

<%@ page import="us.quizz.repository.QuizRepository"%>
<%@ page import="us.quizz.repository.QuizQuestionRepository"%>
<%@ page import="us.quizz.repository.QuizPerformanceRepository"%>

<%@ page import="java.util.List"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.TreeSet"%>
<%@ page import="java.text.NumberFormat"%>

<%
	User user = User.getUseridFromCookie(request, response);
	
	String relation = request.getParameter("relation");
	Quiz quiz = QuizRepository.getQuiz(relation);

	String useranswer = request.getParameter("useranswer");
	if (useranswer == null)
		useranswer = "";
	String gold_prior = request.getParameter("goldprior");
	String isCorrect = request.getParameter("iscorrect");

	
%>
<jsp:include page="/header.jsp"><jsp:param name="title" value="<%=quiz.getName()%>" /></jsp:include>

<body>
	<div class="container" style="text-align: center; max-width:720px">
			<h2><a href="/"><span style="color: maroon">Quizz</span>.us</a></h2>
			

		<div id="feedback">
			
		<%
		if (isCorrect == null) {
			;
		} else 	if (isCorrect.equals("true")) {
			%>
			<div class="alert alert-success" id="showMessage">
			The answer  <span class="label label-success"><%=useranswer%></span> was <span class="label label-success">correct</span>!
			</div>
			<%
		} else {
			%>
			<div class="alert alert-error" id="showMessage">
			The answer <span class="label label-important"><%=useranswer%></span> was <span class="label label-important">incorrect</span>!
			</div>
			<%
		}

		if (gold_prior == null) {
			; 
		} else 	if (user.getsTreatment("showMessage") && isCorrect.equals("false")) {
			%>
			<div class="alert alert-success" id="showCorrect">
			The correct answer was <span class="label label-success"><%=gold_prior%></span>.
			</div>
			<%
		} else 	if (!user.getsTreatment("showMessage")) {
			%>
			<div class="alert alert-success" id="showCorrect">
			The correct answer was <span class="label label-success"><%=gold_prior%></span>.
			</div>
			<%
		}

		String totalanswers = request.getParameter("totalanswers");
		String correctanswers = request.getParameter("correctanswers");

		if (totalanswers != null && correctanswers != null) {
			Integer t = Integer.parseInt(totalanswers) + 1;
			Integer c = Integer.parseInt(correctanswers) + 1;
			Double p = 1.0 * c / t;
			NumberFormat percentFormat = NumberFormat.getPercentInstance();
			percentFormat.setMaximumFractionDigits(0);
			String rate = percentFormat.format(p);
			%>
			<div class="alert alert-info" id="showCrowdAnswers">
			Crowd performance: <span class="label label-info"><%=c.toString()%> out of the <%=t.toString()%> users	 (<%=rate%>) answered correctly.</span>
			</div>
			<%
		} 
%>
</div>
			<div class="alert alert-info" style="text-align: center">
				<span class="label label-info" id="showScore"></span>
				<span class="label label-info" id="showTotalCorrect"></span>
				<span class="label label-info" id="showPercentageCorrect"></span>
				<span class="label label-info"  id="showPercentageRank"></span>
				<span class="label label-info" id="showTotalCorrectRank"></span>
			</div>



		<div class="well" style="text-align: center;">
			<form id="addUserEntry" action="/processUserAnswer" method="post">
					<fieldset>
						<div class="lead">
							<span id="questiontext"></span> 
							<a id="midname" href=""></a>
						</div>
						<div id="answers"></div>
						<input id="relation" name="relation" type="hidden" value=""> 
						<input id="mid" name="mid" type="hidden" value="">
						<input id="gold" name="gold" type="hidden" value=""> 
					</fieldset>
				</form>
			</div>		
	</div>


<%@ include file="assets/google-analytics.html" %>


	<script>

	
	$(document).ready(function() {
		
		$('#feedback').delay(2000).fadeOut();
		
		var user = getUsername();
		var quiz = getURLParameterByName('relation');
		
		// Populate the question elements
		$.when(getUserTreatments(user),	getUserQuizPerformance(quiz, user), getNextQuizQuestion(quiz)).done(function(a1, a2, a3){
			
			// Display the performance scores
			performance = a2[0];
			question = a3[0];
			displayPerformanceScores(quiz, performance);
			populateQuestion(question, performance);
			
			// Show/hide the various elements, according to the user treatments.
			userdata = a1[0];
			$.each(userdata.treatments, function(key, value) {
				var element = $(document.getElementById(key));
				if (value == true) {
					element.show();
				} else {
					element.hide();
				}
			});
		});
	});
	</script>

</body>
</html>