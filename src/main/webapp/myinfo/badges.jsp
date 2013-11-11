<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.BadgeAssignment"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Badge"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="us.quizz.repository.UserRepository"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="List of Earned Badges" /></jsp:include>
<% User u = UserRepository.getUseridFromCookie(request, response); %>

<body>
	<div class="container pagination-centered">
		<div class="row span6 offset4">
			<h2>Earned Badges</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Name</th>
				</tr>
				<%
				PersistenceManager	pm = PMF.getPM();
				Query q = pm.newQuery(BadgeAssignment.class);
				q.setFilter("user == userParam");
				q.declareParameters("User userParam");
				@SuppressWarnings("unchecked")
				List<BadgeAssignment> userBadgeAssignments = (List<BadgeAssignment>) q.execute(u);
				pm.close();
				for (BadgeAssignment ba : userBadgeAssignments) {
					%>
					<tr>
						<td><%=ba.getBadge().getBadgename()%></td>
					</tr>
					<%
				}
				%>
			</table>
		</div>
	</div>
</body>
</html>