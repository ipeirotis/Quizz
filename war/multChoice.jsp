<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="java.util.UUID"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Set"%>

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


<script src="http://malsup.github.com/jquery.form.js"
	type="text/javascript"></script>
	


</head>
<body>
	<div class="container pagination-centered">

		<%
			String relation = request.getParameter("relation");
			String mid = request.getParameter("mid");

			PersistenceManager pm = PMF.get().getPersistenceManager();
			Quiz q = null;
			QuizQuestion eq = null;

			try {
				q = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
				eq = pm.getObjectById(QuizQuestion.class, QuizQuestion.generateKeyFromID(relation, mid));
			} catch (Exception e) {
				q = null;
				eq = null;
			}
		%>
		
		<div class="row">
		<div class="span4 offset4"><a href="/"><h2><span style="color: maroon">Quizz</span>.us</h2></a></div>
		</div>

		<div class="row">
			
			<div class="span8 offset2">


				<form id="addUserEntry" action="/processUserAnswer" method="post"
					style="background-color: #D4D4D4; border-radius: 5px;">
					<fieldset>
						<legend>
							<%=q.getQuestionText()%>
							<a href="http://www.freebase.com<%=mid%>"> 
							<%=FreebaseSearch.getFreebaseAttribute(mid,"name") %>
							</a>
						</legend>
						
						<div class="controls">
						<%
						int choices = 4;
						Set<String> answers = eq.getMultipleChoice(choices);
						if (answers.size()<2) {
							User u = User.getUseridFromCookie(request, response);
							String baseURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
							String nextURL = baseURL + Helper.getNextURL(relation, u.getUserid(), null);
							response.sendRedirect(nextURL);
							return;
						}
						for (String s: answers) {
							%>
							<div class="row">
							<div class="span2 offset3">
							<label class="radio" for="radios-<%=s%>" style="text-align:left">
							<input style="background-color: #EEEEEE; border-radius: 5px;" type="radio" name="useranswer" id="radios-<%=s%>" value="<%=s%>"><%=s%></input>
							</label>
							</div>
							</div>
							<%
						}
						%>
						</div>
						<input id="relation" name="relation" type="hidden" value="<%= relation %>"> 
						<input id="mid" name="mid" type="hidden" value="<%= mid %>">
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

<%@ include file="social-sharing.html" %>
<%@ include file="google-analytics.html" %>

	<script type="text/javascript">
		$( document ).ready(function() {
					
			
		    $('#addUserEntry').ajaxForm({ 
		        // dataType identifies the expected content type of the server response 
		        dataType:  'json', 
		 
		        // success identifies the function to invoke when the server response 
		        // has been received 
		        success:   processJson 
		    }); 
		    


		});


    function processJson(data) {
		
    	// Mark a conversion in Google Analytics
		ga('send', {
			  'hitType': 'event', 
			  'eventCategory': 'quiz-submission', 
			  'eventAction': 'fill-in', 
			  'eventLabel': '<%= q.getRelation() %>',
			  <% if (eq.getWeight()!=null) {
			  	%> 'eventValue': <%= eq.getWeight() %>, <%
			  } else {
			  	
			  }
			  %>
			  
			});
		
		var result = jQuery.parseJSON(data);
		if (data.feedback) {
			alert(data.feedback);
		}
		
        window.location.href = data.url;
        
    }
	

	</script>



</body>
</html>