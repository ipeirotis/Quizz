<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="us.quizz.utils.PMF"%>
<%@ page import="us.quizz.entities.Badge"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>

<jsp:include page="/admin/header.jsp"><jsp:param name="title" value="List of Badges" /></jsp:include>

<body>
	<div class="container pagination-centered">
		<div class="row span6 offset4">
			<h2>Available Badges</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Name</th>
				</tr>
				<%
				PersistenceManager	pm = PMF.getPM();
				Query q = pm.newQuery(Badge.class);
				@SuppressWarnings("unchecked")
				List<Badge> allBadges = (List<Badge>) q.execute();
				pm.close();
				for (Badge b : allBadges) {
					%>
					<tr>
						<td><%=b.getBadgename()%></td>
					</tr>
					<%
				}
				%>
				<tr>
					<td colspan="2" style="text-align: center">
						<a href="create_badge.jsp">Create a new badge</a>
					</td>
				</tr>
			</table>
		</div>
	</div>
</body>
</html>