<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Question"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="java.util.UUID"%>
<%@ page import="java.util.List"%>
<%@ page import="us.quizz.repository.QuizRepository"%>
<%@ page import="us.quizz.repository.QuizQuestionRepository"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>List of Supported Relations</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet"
	media="screen">
<script src="bootstrap/js/bootstrap.min.js" type="text/javascript"></script>

<link type="text/css" rel="stylesheet"
	href="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.css" />
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
<script type="text/javascript"
	src="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.js"></script>



</head>
<body>
	<div class="container pagination-centered">

		<%
			String relation = request.getParameter("relation");
			String mid = request.getParameter("mid");

			
			Quiz q = QuizRepository.getQuiz(relation);
			Question eq = QuizQuestionRepository.getQuizQuestion(relation, mid);
		%>
		<div class="row">
			<div class="span8 offset2">
				<h3>Do you know...</h3>
			</div>
		</div>
		<div class="row">
			<div class="span8 offset2">


				<form id="addUserEntry" action="/processUserAnswer" method="post"
					style="background-color: #D4D4D4; border-radius: 5px;">
					<fieldset>
						<legend>
							<%=q.getQuestionText()%>
							<a href="http://www.freebase.com<%=mid%>"> <%=FreebaseSearch.getFreebaseAttribute(mid, "name")%>
							</a>
						</legend>
						<input id="useranswer" name="useranswer" type="text"> <input
							id="relation" name="relation" type="hidden"
							value="<%=relation%>"> <input id="mid" name="mid"
							type="hidden" value="<%=mid%>">
						<div class="form-actions"
							style="background-color: #D0D0D0; border-radius: 5px;">
							<input type="submit" class="btn" name="action" value="Submit">
							<input type="submit" class="btn" name="action"
								value="I don't know"> <input type="submit" class="btn"
								name="action" value="No such thing">
						</div>
					</fieldset>
				</form>
			</div>
		</div>
	</div>

<%@ include file="assets/social-sharing.html" %>
<%@ include file="assets/google-analytics.html" %>

	<script type="text/javascript">
		$( document ).ready(function() {
			
			 <!-- Add the Freebase Suggest widget on the form to enable autocompletion -->
			<%// if (q.getFreebaseType().startsWith("/type/") || q.getFreebaseType().startsWith("/common/" )) {
			//	;
			//} else { 
			//	$("#useranswer").suggest({
			//		'key' : 'AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc',
			//	    'filter' : '(all type: = q.getFreebaseType() )', 
			//	});
			//}%>
				
			
		    $('#addUserEntry').ajaxForm({ 
		        // dataType identifies the expected content type of the server response 
		        dataType:  'json', 
		 
		        // success identifies the function to invoke when the server response 
		        // has been received 
		        success:   processJson 
		    }); 
		    


		});


    function processJson(data) {
		
    	
    	// Sends notification for conversion to Google Analytics
		ga('send', {
			  'hitType': 'event', 
			  'eventCategory': 'quiz-submission', 
			  'eventAction': 'fill-in', 
			  'eventLabel': '<%=q.getRelation()%>',
			  <%if (eq.getWeight() != null) {%> 'eventValue': <%=eq.getWeight()%>, <%} else {

			}%>
			  
			});
		
		var result = jQuery.parseJSON(data);
		if (data.message) {
			alert(data.message);
		}
		
        window.location.href = data.url;
        
    }
	

	</script>



</body>
</html>