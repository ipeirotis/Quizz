<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="us.quizz.utils.PMF"%>
<%@ page import="us.quizz.entities.BadgeAssignment"%>
<%@ page import="us.quizz.entities.Badge"%>
<%@ page import="us.quizz.entities.User"%>
<%@ page import="us.quizz.repository.UserRepository"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>
<%@ page import="com.google.inject.Injector"%>
<%@ page import="com.google.inject.Guice"%>
<%@ page import="us.quizz.di.CommonModule"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="List of Earned Badges" /></jsp:include>
<% 
Injector i = Guice.createInjector(new CommonModule());
UserRepository userRepository = i.getInstance(UserRepository.class);
User u = userRepository.getUseridFromCookie(request, response); 
%>

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
				q.setFilter("userid == useridParam");
				q.declareParameters("String useridParam");
				@SuppressWarnings("unchecked")
				List<BadgeAssignment> userBadgeAssignments = (List<BadgeAssignment>) q.execute(u.getUserid());
				pm.close();
				for (BadgeAssignment ba : userBadgeAssignments) {
					%>
					<tr>
						<td><%=ba.getBadgename()%></td>
					</tr>
					<%
				}
				%>
			</table>
		</div>
	</div>
</body>
</html>