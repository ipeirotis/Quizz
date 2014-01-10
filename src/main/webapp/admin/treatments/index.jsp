<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="us.quizz.utils.PMF"%>
<%@ page import="us.quizz.entities.Treatment"%>
<%@ page import="javax.jdo.Query"%>
<%@ page import="java.util.List"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="List of available treatments" /></jsp:include>

<body>
	<div class="container pagination-centered">
		<div class="row span6 offset4">
			<h2>Available treatments</h2>
			<table class="table table-striped  table-bordered">
				<tr>
					<th>Name</th>
					<th>Probability</th>
				</tr>
				<%
				PersistenceManager	pm = PMF.getPM();
				Query q = pm.newQuery(Treatment.class);
				@SuppressWarnings("unchecked")
				List<Treatment> allTreatments = (List<Treatment>) q.execute();
				pm.close();
				for (Treatment t : allTreatments) {
					%>
					<tr>
						<td><%=t.getName()%><br>
						<td><%=t.getProbability()%></td>
					</tr>
					<%
				}
				%>
				<tr>
					<td colspan="2" style="text-align: center">
						<a href="create_treatment.jsp">Create a new treatment</a>
					</td>
				</tr>
			</table>
		</div>
	</div>
</body>
</html>