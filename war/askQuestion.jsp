<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.adcrowdkg.PMF"%>
<%@ page import="com.ipeirotis.adcrowdkg.Question"%>
<%@ page import="com.ipeirotis.adcrowdkg.EntityQuestion"%>
<%@ page import="com.ipeirotis.adcrowdkg.FreebaseSearch"%>
<%@ page import="java.util.List"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>List of Supported Relations</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
<script src="bootstrap/js/bootstrap.min.js" type="text/javascript"></script>

<link type="text/css" rel="stylesheet" href="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.css" />
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
<script type="text/javascript" src="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.js"></script>

<script src="http://malsup.github.com/jquery.form.js"></script> 

</head>
<body>
	<div class="container pagination-centered" >
		<div class="row">
		   <div class="span8 offset2">
			<h3>Type the answer once, have the answer for ever</h3>
			</div>
		</div>
		<div class="row">
			<div class="span8 offset2">
			<%
				String relation = request.getParameter("relation");
				String mid = request.getParameter("mid");
				
				PersistenceManager pm = PMF.get().getPersistenceManager();
				Question q = null;
				
				try {
					q = pm.getObjectById(Question.class, Question.generateKeyFromID(relation));
		    	} catch (Exception e) {
		        	q = null;
		    	}
				
			%>
			<form id="addUserEntry" action="/addUserEntry" style="background-color: #D4D4D4; border-radius: 5px;">
			  <fieldset>
			    <legend>
			    	<%= q.getQuestionText() %> <%= FreebaseSearch.getFreebaseName(mid).toLowerCase() %>
			    </legend>
			    <input id="useranswer" name="useranswer" type="text">
			    <input id="freebaseanswer" name="freebaseanswer" type="hidden">
			    <input id="relation" name="relation" type="hidden" value="<%= relation %>">
			    <input id="mid" name="mid" type="hidden" value="<%= mid %>">
			    <div class="form-actions" style="background-color: #D0D0D0; border-radius: 5px;">
			    <button type="submit" class="btn">Submit</button>
			    </div>
			  </fieldset>
			</form>
			</div>
		</div>
	</div>
	
	<script type="text/javascript">
		$( document ).ready(function() {
			
			
			
			<!-- Populate the field with the existing answer from Freebase, if any -->
			 queryFreebase('<%= mid %>', $("input[name=freebaseanswer]"));

			<% if (q.getFreebaseType().equals("/type/float")) {
				;
			} else { %>	
				$("#useranswer").suggest({
					'key' : 'AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc',
				    'filter' : '(all type:<%= q.getFreebaseType() %>)', 
				});
			<%	
			}
			%>
				
			$('#addUserEntry').ajaxForm(function() { 
                alert("Thank you for your entry!"); 
            }); 
		});


	</script>

	<script type="text/javascript">
	<!-- For all table cells with the name FreebaseName, take the id of the cell, 	  -->
	<!-- query Freebase, and replace its content with the name of the Freebase entity -->
		
		

		function queryFreebase(freebaseMid, element) {
			
			var query = {                         
					'mid'               : freebaseMid,  
					'name'				: null,
					'<%= q.getFreebaseAttribute() %>' : null,
				  };
			
			 var params = {
					    'key'   : 'AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc',
					    'limit' : 1,
						'exact' : true,
					    'query' : JSON.stringify(query)
					  };
			
			url = 'https://www.googleapis.com/freebase/v1/mqlread';
			
			$.getJSON(url + '?callback=?',  params, function(response) {
				updateValue(response, element)
			});
		}

		function updateValue(response, element) {
			element.val(response.result['<%= q.getFreebaseAttribute() %>']);
		}
	</script>



</body>
</html>