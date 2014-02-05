<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<jsp:include page="/admin/header.jsp"><jsp:param name="title" value="Create new badge" /></jsp:include>

<body>

	<div class="container">

		<form class="form-horizontal" id="createBadge"
			action="/addBadge" method="post"
			style="background-color: #D4D4D4; border-radius: 5px; padding: 10px"
			>
			<fieldset>
				<legend style="text-align: center">Create a new badge</legend>
				<div class="control-group">
					<label class="control-label" for="name">Name:</label>
					<div class="controls">
					<input class="input-large" id="name" name="name" type="text">
					</div>
					<label class="control-label" for="sname">Short Name:</label>
					<div class="controls">
					<input class="input-large" id="sname" name="sname" type="text">
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