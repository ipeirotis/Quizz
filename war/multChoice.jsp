<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.*"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>

<%@ page import="java.util.UUID"%>
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

	PersistenceManager pm = PMF.get().getPersistenceManager();
	Quiz quiz = null;
	QuizQuestion question = null;
	QuizPerformance performance = null;

	try {
		quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
		question = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(relation, mid));
	} catch (Exception e) {
		String nextURL = Helper.getNextMultipleChoiceURL(request, relation, user.getUserid(), null);
		response.sendRedirect(nextURL);
	}
	
	try {
		performance = CachePMF.get("qp_"+user.getUserid()+"_"+relation, QuizPerformance.class);
		if (performance == null) 
			performance = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(relation, user.getUserid()));
	} catch (Exception e) {
		//performance = new QuizPerformance(relation, u.getUserid());
		//pm.makePersistent(performance);
	}
	
	pm.close();
	
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
		%>


		<%
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

	if (performance!=null) {
%>
			<div class="alert alert-info" style="text-align: center">
				<span class="label label-info" id="showScore">
					Score: <%=performance.displayScore()%> points
				</span>
			
				<span class="label label-info" id="showTotalCorrect">
					Correct Answers: <%=performance.getCorrectanswers()%>/<%=performance.getTotalanswers()%>
				</span>
				<span class="label label-info" id="showPercentageCorrect">
					Correct (%): <%=performance.displayPercentageCorrect()%>
				</span>
				<span class="label label-info"  id="showPercentageRank">
					Rank (%correct): <%=performance.getRankPercentCorrect()%>/<%=performance.getTotalUsers()%> (Top-<%=performance.displayRankPercentageCorrect()%>)
				</span>
				<span class="label label-info" id="showTotalCorrectRank">
					Rank (#correct): <%=performance.getRankTotalCorrect()%>/<%=performance.getTotalUsers()%> (Top-<%=performance.displayRankTotalCorrect()%>)
				</span>

				
			</div>

		<%
	}
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


    function markConversion(type, value) {
    	// Mark a conversion in Google Analytics
		ga('send', {
			  'hitType': 'event', 
			  'hitCallback': function(){ },
			  'eventCategory': 'quiz-submission', 
			  'eventAction': type, 
			  'eventLabel': '<%= quiz.getRelation() %>',
			  'eventValue': value, 
			  } );
    }
	
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


	<%
if (user.getsTreatment("showPercentageRank")) { 
	%>$('#showPercentageRank').show();
	<%
} else {
	%>$('#showPercentageRank').hide();
	<%
}

if (user.getsTreatment("showTotalCorrectRank")) { 
	%>$('#showTotalCorrectRank').show();
	<%
} else {
	%>$('#showTotalCorrectRank').hide(); 
	<%
}

if (user.getsTreatment("showTotalCorrect")) { 
	%>$('#showTotalCorrect').show();
	<%
} else {
	%>$('#showTotalCorrect').hide(); 
	<%
}

if (user.getsTreatment("showPercentageCorrect")) { 
	%>$('#showPercentageCorrect').show();
	<%
} else {
	%>$('#showPercentageCorrect').hide();
	<%
}

if (user.getsTreatment("showMessage")) { 
	%>$('#showMessage').show();  $('#showMessage').delay(5000).fadeOut();
	<%
} else {
	%>$('#showMessage').hide();
	<%
}

if (user.getsTreatment("showCorrect")) { 
	%>$('#showCorrect').show();  $('#showCorrect').delay(5000).fadeOut();
	<%
} else {
	%>$('#showCorrect').hide();
	<%
}

if (user.getsTreatment("showCrowdAnswers")) { 
	%>$('#showCrowdAnswers').show(); $('#showCrowdAnswers').delay(5000).fadeOut();
	<%
} else {
	%>$('#showCrowdAnswers').hide();
	<%
}

%>
</script>






</body>
</html>