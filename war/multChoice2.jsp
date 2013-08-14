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
	String mid = request.getParameter("mid");
	String numoptions = request.getParameter("numoptions");
	if (numoptions == null) numoptions = "4";

	Quiz quiz = QuizRepository.getQuiz(relation);
	QuizQuestion question = QuizQuestionRepository.getQuizQuestion(relation, mid);
	if (question==null) {
		String nextURL = Helper.getNextMultipleChoiceURL(request, relation, user.getUserid(), null);
		response.sendRedirect(nextURL);
	}
	QuizPerformance performance = QuizPerformanceRepository.getQuizPerformance(relation, user.getUserid());
	
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

%>

		<div class="well" style="text-align: center;">
			<form id="addUserEntry" action="/processUserAnswer" method="post">
					<fieldset>
						<div class="lead">
							<%=quiz.getQuestionText()%>
							<a href="http://www.freebase.com<%=mid%>"> 
							<%=FreebaseSearch.getFreebaseAttribute(mid,"name")%>
							</a>
						</div>
						
						<%
							Set<String> answers = new TreeSet<String>();

							String gold = question.getRandomGoldAnswer();
							answers.add(gold);

							int choices = Integer.parseInt(numoptions);
							Set<String> pyrite = question.getIncorrectAnswers(choices-1);
							answers.addAll(pyrite);
							
							if (answers.size()<2) {
								String nextURL = Helper.getNextMultipleChoiceURL(request, relation, user.getUserid(), null);
								response.sendRedirect(nextURL);
								return;
							}
							
							int i=0;
							for (String s: answers) {
							%>
							<input id="useranswer<%=i %>" name="useranswer<%=i %>" type="submit" class="btn btn-primary btn-block" value="<%=s%>">
							<%
							i++;
							}
							
						%>
						<input id="idk_button" type="submit" class="btn btn-danger btn-block" name="idk" value="I don't know">
						<input id="numoptions" name="numoptions" type="hidden" value="<%= numoptions %>">
						<input id="relation" name="relation" type="hidden" value="<%= relation %>"> 
						<input id="mid" name="mid" type="hidden" value="<%= mid %>">
						<input id="gold" name="gold" type="hidden" value="<%= gold %>"> 
						
					</fieldset>
				</form>
			</div>
		
			
	</div>


<%@ include file="assets/google-analytics.html" %>

	<script type="text/javascript">
	
    $(document).ready( function() { <%
    	i=0;
    	for (String s: answers) {
    		if (s.equals(gold)) {
    		%>
    			$("#useranswer<%=i %>").mousedown(function(e){
    				markConversion("multiple-choice-correct", "<%=(performance!=null)?Math.round(performance.getScore()):0 %>");
    			});
    		<%
    			} else {
    		%>
    			$("#useranswer<%=i %>").mousedown(function(){
    				markConversion("multiple-choice-incorrect", "<%=(performance!=null)?Math.round(performance.getScore()):0 %>");
    			});
    		<%		
    		}
    	i++;
    	}
    	%>
    	$("#idk_button").mousedown(function(){
    		markConversion("multiple-choice-idk", 0);
    	});
    });

	</script>

	<script>

	
	$(document).ready(function() {
		
		$('#feedback').delay(2000).fadeOut();
		
		var user = getUsername();
		var quiz = getURLParameterByName('relation');
		$.when(getUserTreatments(user), getUserQuizPerformance(quiz, user)).done(function(a1, a2){
			
			// Display the performance scores
			performance = a2[0];
			displayPerformanceScores(quiz, performance);
			
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