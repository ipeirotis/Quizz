<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="User info" /></jsp:include>

<body>
	<div class="container pagination-centered">
		<div class="row span8 offset2">
		<h2>User info</h2>
			<table class="table table-striped  table-bordered">
				<tr><th colspan="2">User Information</th></tr>
				<% User u = User.getUseridFromCookie(request, response); %>
				<tr><td  class="span2">Userid:</td> <td><%=u.getUserid() %></td></tr>
				<tr><th colspan="2">Treatments</th></tr>
				<% 
				Map<String, Boolean> treatments = u.getTreatments();
				for (String name : treatments.keySet()) {
					%> 
					<tr>
						<td  class="span2"><%= name %></td>
						<td class="span6"><%= treatments.get(name) %></td>
					</tr>
					<%	
				}
				%>
			</table>
			

		</div>
	</div>

</body>
</html>