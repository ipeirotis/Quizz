<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.adcrowdkg.PMF"%>
<%@ page import="com.ipeirotis.adcrowdkg.Question"%>
<%@ page import="com.ipeirotis.adcrowdkg.EntityQuestion"%>
<%@ page import="com.ipeirotis.adcrowdkg.FreebaseSearch"%>
<%@ page import="java.util.List"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>List of Supported Relations</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
<script src="jquery/jquery.js" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
</head>
<body>
	<div class="container pagination-centered">
		<table class="table table-striped  table-bordered">

			<%
				String relation = request.getParameter("relation");
			%>
			<tr>
				<th colspan="2">Questions for Relation <%=relation%></th>
				
			</tr>
			<%
				PersistenceManager pm = PMF.get().getPersistenceManager();
				String query = "select from " + EntityQuestion.class.getName() + " where relation=='"+relation+"'";
				List<EntityQuestion> questions = (List<EntityQuestion>) pm.newQuery(query).execute();
				if (questions.isEmpty()) {
			%>
			<tr>
				<td>No entries found!</td>
			</tr>
			<%
				} else {
					for (EntityQuestion q: questions) {
			%>
			<tr>
				<td><a target="_blank" href="http://www.freebase.com<%=q.getFreebaseEntityId()%>"><div  name="FreebaseName" id="<%=q.getFreebaseEntityId()%>">Loading...</div></a></td>
				<td><a href="/askQuestion.jsp?mid=<%=q.getFreebaseEntityId()%>&relation=<%=q.getRelation()%>">Ask question</a></td>
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
	<!-- query Freebase, and replace its content with the name of the Freebase entity -->
		$('div[name^="FreebaseName"]').each(function(index) {
			queryFreebase($(this).attr('id'), $(this));
		});

		function queryFreebase(freebaseMid, element) {
			
			var query = {                         
					'mid'               : freebaseMid,  
					'name'              : null,
				  };
			
			 var params = {
					    'key'   : 'AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc',
					    'limit' : 1,
						'exact' : true,
					    'query' : JSON.stringify(query)
					  };
			
			url = 'https://www.googleapis.com/freebase/v1/mqlread';
			
			$.getJSON(url + '?callback=?',  params, function(response) {
				updateNames(response, element)
			});
		}

		function updateNames(response, element) {
			element.html(response.result.name);
		}
	</script>

</body>
</html>