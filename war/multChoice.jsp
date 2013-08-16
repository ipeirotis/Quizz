<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:include page="/header.jsp"></jsp:include>

<body>
	<div class="container" style="text-align: center; max-width:720px">

		<h2>
			<a href="/"><span style="color: maroon">Quizz</span>.us</a>
		</h2>
	
		<div id="feedback" style="display: none;">
			<div class="alert alert-success" id="showMessage"></div>
			<div class="alert alert-success" id="showCorrect"></div>
			<div class="alert alert-info" id="showCrowdAnswers"></div>
		</div>
		
		<div id="scores" class="alert alert-info" style="text-align: center; display: none;">
			<span class="label label-info" id="showScore"></span>
			<span class="label label-info" id="showTotalCorrect"></span>
			<span class="label label-info" id="showPercentageCorrect"></span>
			<span class="label label-info"  id="showPercentageRank"></span>
			<span class="label label-info" id="showTotalCorrectRank"></span>
		</div>
	
		<div id="form" class="well" style="text-align: center;">
			<form id="addUserEntry" action="/processUserAnswer" method="post">
				<fieldset>
					<div class="lead">
						<span id="questiontext"></span> 
						<a id="midname" href=""></a>
					</div>
					<div id="answers"></div>
					<input id="relation" name="relation" type="hidden" value=""> 
					<input id="mid" name="mid" type="hidden" value="">
					<input id="totalanswers" name="totalanswers" type="hidden" value=""> 
					<input id="correctanswers" name="correctanswers" type="hidden" value=""> 
					<input id="gold" name="gold" type="hidden" value=""> 
				</fieldset>
			</form>
		</div>		
	</div>

	<script>
	$(document).ready(function() {
		
		$('#feedback').hide();
		$('#scores').hide();
		$('#form').hide();
		
		var user = getUsername();
		var quiz = getURLParameterByName('relation');
		var mid = getURLParameterByName('mid');
		var prior = getURLParameterByName('prior');
				
		getUserTreatments(user);
		getUserQuizPerformance(quiz, user); 
		getNextQuizQuestion(quiz);
		getFeedbackForPriorAnswer(user, quiz, prior);
		
		$('#feedback').show();
		$('#scores').show();
		$('#form').show();
		$('#feedback').delay(5000).fadeOut();

	});
	</script>

<%@ include file="assets/google-analytics.html" %>


</body>
</html>