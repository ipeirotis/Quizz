<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="us.quizz.utils.PMF"%>
<%@ page import="us.quizz.entities.Quiz"%>
<%@ page import="us.quizz.entities.Question"%>
<%@ page import="us.quizz.utils.FreebaseSearch"%>
<%@ page import="java.util.List"%>

<jsp:include page="../header.jsp"><jsp:param name="title" value="Create new treatment" /></jsp:include>

<body>

	<div class="container">

		<form class="form-horizontal" id="createTreatment"
			action="/addTreatment" method="post"
			style="background-color: #D4D4D4; border-radius: 5px; padding: 10px"
			>
			<fieldset>
				<legend style="text-align: center">Create a new treatment</legend>
				<div class="control-group">
					<label class="control-label" for="name">Name:</label>
					<div class="controls">
						<input class="input-large" id="name" name="name" type="text">
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="probability">Probability:</label>
					<div class="controls">
						<input class="input-large" id="probability" name="probability" type="text">
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