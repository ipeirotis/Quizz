<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="java.util.UUID"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.TreeSet"%>
<%@ page import="java.text.NumberFormat"%>



<%
	User user = User.getUseridFromCookie(request, response);
	String relation = request.getParameter("relation");
	String mid = request.getParameter("mid");

	PersistenceManager pm = PMF.get().getPersistenceManager();
	Quiz quiz = null;
	QuizQuestion question = null;
	QuizPerformance performance = null;

	try {
		quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
		question = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(relation, mid));
	} catch (Exception e) {
		String baseURL = request.getScheme() + "://" + request.getServerName(); 
		String nextURL = baseURL + Helper.getNextMultipleChoiceURL(relation, user.getUserid(), null);
		response.sendRedirect(nextURL);
	}
	
	try {
		performance = pm.getObjectById(QuizPerformance.class, QuizPerformance.generateKeyFromID(relation, user.getUserid()));
	} catch (Exception e) {
		//performance = new QuizPerformance(relation, u.getUserid());
		//pm.makePersistent(performance);
	}
	
	
	pm.close();
%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="<%=quiz.getName()%>" /></jsp:include>



</head>
<body>
	<div class="container pagination-centered">


		
		<div class="row-fluid">
			<div class="span12" style="text-align:center"><a href="/"><h2><span style="color: maroon">Quizz</span>.us</h2></a></div>
		</div>

		<div class="row-fluid " id="showMessage">
				<div  class="span12" style="font-size:small;background-color: #E4E4E4; border-radius: 5px;">
				<%
				String useranswer = request.getParameter("useranswer");
				if (useranswer==null) useranswer="";
				String gold_prior = request.getParameter("goldprior");
				if (gold_prior==null) gold_prior="";
				String isCorrect = request.getParameter("iscorrect");
				if (isCorrect==null) isCorrect="";
				if (isCorrect.equals("true")) {
					%> The answer  <b><%=useranswer%></b> was <span style="color: green">correct</span>! <%
				} else { 
					%> The answer  <b><%=useranswer%></b> was <span style="color: red">incorrect</span>!<%
				}
				%>
				</div>
			</div><div class="row-fluid" id="showCorrect">	
				<div  class="span12" style="font-size:small;background-color: #E4E4E4; border-radius: 5px;">
				<%
				if (user.getsTreatment("showMessage") && isCorrect.equals("false")) {
					%>The correct answer was <span style="color: green"><%=gold_prior %></span>.<%	
				}
				if (!user.getsTreatment("showMessage")) {
					%>The correct answer was <span style="color: green"><%=gold_prior %></span>.<%	
				}
				%>
				</div>
			</div><div class="row-fluid" id="showCrowdAnswers">		
				
				<div  class="span12" style="font-size:small;background-color: #E4E4E4; border-radius: 5px;">
				<%
				String totalanswers = request.getParameter("totalanswers");
				String correctanswers = request.getParameter("correctanswers");

				if (totalanswers!=null && correctanswers!=null) {
					Integer t = Integer.parseInt(totalanswers)+1;
					Integer c = Integer.parseInt(correctanswers)+1;
					Double p = 1.0*c/t;
					NumberFormat percentFormat = NumberFormat.getPercentInstance();
					percentFormat.setMaximumFractionDigits(0);
					String rate = percentFormat.format(p);
					%> 
					The success rate in the previous question was <%=rate %>. Out of the <%=t.toString() %> users, <%=c.toString() %> answered correctly. 
					<%
				} 
				%>
				</div>
		</div>

<%
	if (performance!=null) {
%>
		<div class="row-fluid" style="color:maroon;font-size:small;background-color: #F4F4F4; border-radius: 5px; text-align: center" >
				<div class="span3" style="display: inline-block" id="showTotalCorrect">#Correct<br><%=performance.getCorrectanswers()%>/<%=performance.getTotalanswers()%></div>
				<div class="span3" style="display: inline-block"  id="showPercentageCorrect">Correct (%)<br><%=performance.displayPercentageCorrect()%></div>
				<div class="span3" style="display: inline-block" id="showPercentageRank">Rank (%correct)<br><%=performance.getRankPercentCorrect()%>/<%=performance.getTotalUsers()%> (Top-<%=performance.displayRankPercentageCorrect()%>)</div>
				<div class="span3" style="display: inline-block" id="showTotalCorrectRank">Rank (#correct)<br><%=performance.getRankTotalCorrect()%>/<%=performance.getTotalUsers()%> (Top-<%=performance.displayRankTotalCorrect()%>)</div>
		</div>
<%
	}
%>

		<div class="row-fluid">
			<div class="span12">
				<form id="addUserEntry" action="/processUserAnswer" method="post"
					style="background-color: #D4D4D4; border-radius: 5px;">
					<fieldset>
						<legend>
							<%=quiz.getQuestionText()%>
							<a href="http://www.freebase.com<%=mid%>"> 
							<%=FreebaseSearch.getFreebaseAttribute(mid,"name")%>
							</a>
						</legend>
						
						<div class="controls">
						<%
							int choices = 4;

										Set<String> answers = new TreeSet<String>();

										String gold = question.getRandomGoldAnswer();
										answers.add(gold);

										Set<String> pyrite = question.getIncorrectAnswers(choices-1);
										answers.addAll(pyrite);
										
										if (answers.size()<2) {
											String baseURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
											String nextURL = baseURL + Helper.getNextMultipleChoiceURL(relation, user.getUserid(), null);
											response.sendRedirect(nextURL);
											return;
										}
										for (String s: answers) {
						%>
							<div class="row-fluid">
							<div class="span12">
							<label class="radio" for="radios-<%=s%>" style="text-align:left">
							<input style="background-color: #EEEEEE; border-radius: 5px;" type="radio" name="useranswer" id="radios-<%=s%>" value="<%=s%>"><%=s%>
							</label>
							</div>
							</div>
							<%
						}
						%>
						</div>
						<input id="relation" name="relation" type="hidden" value="<%= relation %>"> 
						<input id="mid" name="mid" type="hidden" value="<%= mid %>">
						<input id="gold" name="gold" type="hidden" value="<%= gold %>"> 
						
						<div class="form-actions"
							style="background-color: #D0D0D0; border-radius: 5px;">
							<input type="submit" class="btn" name="action" value="Submit">
							<input type="submit" class="btn" name="action" value="I don't know">
						</div>
					</fieldset>
				</form>
			</div>
		</div>
		
		
			
	</div>


	<script type="text/javascript">


    function processJson(data) {
		
    	// Mark a conversion in Google Analytics
		ga('send', {
			  'hitType': 'event', 
			  'eventCategory': 'quiz-submission', 
			  'eventAction': 'fill-in', 
			  'eventLabel': '<%= quiz.getRelation() %>',
			  <% if (question.getWeight()!=null) {
			  	%> 'eventValue': <%= question.getWeight() %>, <%
			  } else {
			  	
			  }
			  %>
			  
			});
    }
	

	</script>

<script>
<%
if (user.getsTreatment("showPercentageRank")) { 
	%>$('#showPercentageRank').show();<%
} else {
	%>$('#showPercentageRank').hide();<%
}

if (user.getsTreatment("showTotalCorrectRank")) { 
	%>$('#showTotalCorrectRank').show();<%
} else {
	%>$('#showTotalCorrectRank').hide(); <%
}

if (user.getsTreatment("showTotalCorrect")) { 
	%>$('#showTotalCorrect').show();<%
} else {
	%>$('#showTotalCorrect').hide(); <%
}

if (user.getsTreatment("showPercentageCorrect")) { 
	%>$('#showPercentageCorrect').show();<%
} else {
	%>$('#showPercentageCorrect').hide();<%
}

if (user.getsTreatment("showMessage")) { 
	%>$('#showMessage').show();  $('#showMessage').delay(5000).fadeOut();<%
} else {
	%>$('#showMessage').hide();<%
}

if (user.getsTreatment("showCorrect")) { 
	%>$('#showCorrect').show();  $('#showCorrect').delay(5000).fadeOut();<%
} else {
	%>$('#showCorrect').hide();<%
}

if (user.getsTreatment("showCrowdAnswers")) { 
	%>$('#showCrowdAnswers').show(); $('#showCrowdAnswers').delay(5000).fadeOut();<%
} else {
	%>$('#showCrowdAnswers').hide();<%
}

%>
</script>


<%@ include file="social-sharing.html" %>
<%@ include file="google-analytics.html" %>


</body>
</html>