<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Question"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.UserEntry"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.EntityQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="java.util.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<%
				String relation = request.getParameter("relation");
%>

<title>Unanswered questions for quiz <%=relation%></title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet"
	media="screen">
<script src="jquery/jquery.js" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap.min.js" type="text/javascript"></script>


</head>
<body>
	<div class="container pagination-centered">
		<table class="table table-striped  table-bordered">

			<% 
				// Get an array of Cookies associated with this domain
				String userName = null;
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
				   for (Cookie c: cookies) {
				  	 if (c.getName().equals("username")) {
				  		 userName = c.getValue();
				  		 break;
				  	 }
				   }
				} 
				
				if (userName == null) {
					userName = UUID.randomUUID().toString();;
				}
				Cookie username = new Cookie("username", userName);
				response.addCookie( username );
				
			%>
			<tr>
				<th colspan="2">Questions for quiz <%=relation%></th>

			</tr>
			<%
				PersistenceManager pm = PMF.get().getPersistenceManager();
							
					String queryGivenAnswers = "SELECT from "  + UserEntry.class.getName() + 
							" where userid=='" + userName +"' && relation=='"+relation+	"'";
							//System.out.println(queryGivenAnswers);
					List<UserEntry> answers = (List<UserEntry>) pm.newQuery(queryGivenAnswers).execute();
					Set<String> entries = new HashSet<String>();
					for (UserEntry ue: answers) {
						entries.add(ue.getMid());
					}
					
							
					String query = "select from " + EntityQuestion.class.getName() + " where relation=='"+relation+	"'" 
						+"  order by emptyweight DESC";
					System.out.println(query);
					List<EntityQuestion> questions = (List<EntityQuestion>) pm.newQuery(query).execute();
					if (questions.isEmpty()) {
			%>
			<tr>
				<td>No entries found!</td>
			</tr>
			<%
				} else {
						for (EntityQuestion q: questions) {
							if (entries.contains(q.getFreebaseEntityId())) continue;
			%>
			<tr>
				<td><a target="_blank"
					href="http://www.freebase.com<%=q.getFreebaseEntityId()%>"><div
							name="FreebaseName" id="<%=q.getFreebaseEntityId()%>">Loading...</div></a></td>
				<td><a
					href="/askQuestion.jsp?mid=<%=q.getFreebaseEntityId()%>&relation=<%=q.getRelation()%>">Give an answer</a></td>
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
		$('div[name^="FreebaseName"]').each(function(index) {
			queryFreebase($(this).attr('id'), $(this));
		});

		function queryFreebase(freebaseMid, element) {

			var query = {
				'mid' : freebaseMid,
				'name' : null,
			};

			var params = {
				'key' : 'AIzaSyAP0fH9aEndZbSDFT87g46YY0gjhkQY8Zc',
				'limit' : 1,
				'exact' : true,
				'query' : JSON.stringify(query)
			};

			url = 'https://www.googleapis.com/freebase/v1/mqlread';

			$.getJSON(url + '?callback=?', params, function(response) {
				updateNames(response, element)
			});

		}

		function updateNames(response, element) {
			element.html(response.result.name);
		}
	</script>
	<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-89122-22', 'crowd-power.appspot.com');
  ga('send', 'pageview');

</script>
</body>
</html>