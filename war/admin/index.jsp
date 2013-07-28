<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="java.util.List"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Admin actions" /></jsp:include>

<body>
	<div class="container">
		<div class="row">
		
			<table class="span6 table table-striped  table-bordered">
				<tr>
					<th>Available actions</th>
				</tr>
				<tr>
					<td>
						<a href="manage/create_quiz.jsp">Create a new quiz</a>
					</td>
				</tr>
				<tr>
					<td>
						<a href="manage/index.jsp">Upload/download data from existing quizzes</a>
					</td>
				</tr>
				<tr>
					<td>
						<a href="treatments/create_treatment.jsp">Create new treatment</a>
					</td>
				</tr>				
				<tr>
					<td>
						<a href="treatments/index.jsp">See available treatments</a>
					</td>
				</tr>
				<tr>
					<td>
						<a href="reports/conversion_rate.jsp">Conversion rate</a>
					</td>
				</tr>	
				<tr>
				<td>
						<a href="reports/contribution_quality.jsp">Quality of contributions</a>
					</td>
				</tr>					
			</table>
			
		</div>
	</div>


</body>
</html>