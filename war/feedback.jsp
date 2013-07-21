<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.User"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizPerformance"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.Helper"%>
<%@ page import="java.util.UUID"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.TreeSet"%>
<%@ page import="java.text.NumberFormat"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Feedback" /></jsp:include>

<% User user = User.getUseridFromCookie(request, response); %>

</head>
<body>
	<div class="container pagination-centered">

		<div class="row">
			
			<div class="span10 offset1">

				<div id="showMessage">
				<%
				String useranswer = request.getParameter("useranswer");
				String gold = request.getParameter("gold");
				if (request.getParameter("iscorrect").equals("true")) {
					%> The answer  <%=useranswer%> is correct! <%
				} else { 
					%> The answer  <%=useranswer%> is incorrect!<%
				}
				%>
				</div>
				
				<div id="showCorrect">
				<%
				if (!request.getParameter("iscorrect").equals("true")) {
					%>The correct answer is <%=gold %>.<%	
				}
				%>
				</div>
				
				
				<div id="showCrowdAnswers">
				<%
				String totalanswers = request.getParameter("totalanswers");
				String correctanswers = request.getParameter("correctanswers");

				if (totalanswers!=null && correctanswers!=null) {
					Integer t = Integer.parseInt(totalanswers)+1;
					Integer c = Integer.parseInt(correctanswers)+1;
					Double p = 1.0*c/t;
					NumberFormat percentFormat = NumberFormat.getPercentInstance();
					percentFormat.setMaximumFractionDigits(0);
					String rate = percentFormat.format(p);
					%> 
					The success rate in this question is <%=rate %>.<br>
					Out of the <%=t.toString() %> users, <%=c.toString() %> answered correctly. 
					<%
				} 
				%>
				</div>
				
				<div id="next">
				<a style="text-align:center;font-size:large" href="<%=request.getParameter("url") %>">NEXT</a>
				</div>
			</div>
		</div>
	</div>

<script>
<%
if (user.getsTreatment("showMessage")) { 
	%>$('#showMessage').show();<%
} else {
	%>$('#showMessage').hide();<%
}

if (user.getsTreatment("showCorrect")) { 
	%>$('#showCorrect').show();<%
} else {
	%>$('#showCorrect').hide(); <%
}

if (user.getsTreatment("showCrowdAnswers")) { 
	%>$('#showCrowdAnswers').show();<%
} else {
	%>$('#showCrowdAnswers').hide(); <%
}

%>
</script>


</body>
</html>