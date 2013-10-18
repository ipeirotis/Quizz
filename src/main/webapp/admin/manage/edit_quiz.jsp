<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>


	<%
	Quiz quiz = null;
	PersistenceManager pm = PMF.getPM();
	String relation = request.getParameter("relation");
	try {
		quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(relation));
	} catch (Exception e) {

	}
	%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Edit quiz '<%=quiz.getName() %>' (<%=quiz.getQuizID() %>)" /></jsp:include>

<body>

	<div class="container">
		<form class="form-horizontal" id="createQuiz"
			action="/addQuiz" method="post"
			style="background-color: #D4D4D4; border-radius: 5px; padding: 10px">
			<fieldset>
				<legend style="text-align: center">Edit quiz</legend>
				
				<div class="control-group">
					<label class="control-label" for="relation">Relation:</label>
					<div class="controls">
						<input class="input-xxlarge" id="relation" name="relation"
							type="text" value="<%=relation %>" readonly>
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="category">Category:</label>
					<div class="controls">
						<input class="input-xxlarge" id="category" name="category"
							type="text" value="<%="category" %>">
					</div>
				</div>			
				
				<div class="control-group">
					<label class="control-label" for="name">Name:</label>
					<div class="controls">
						<input class="input-xxlarge" id="name" name="name" type="text" value="<%=quiz.getName() %>">
					</div>
				</div>


				<div class="control-group">
					<label class="control-label" for="text">Question Text:</label>
					<div class="controls">
						<input class="input-xxlarge" id="text" name="text" type="text"  value="<%=quiz.getQuestionText() %>">
					</div>
				</div>
				
				<div class="form-actions"
					style="background-color: #D0D0D0; border-radius: 5px;">
					<button type="submit" class="btn">Submit</button>	
				</div>
			</fieldset>
		</form>
	</div>
</body>
</html>