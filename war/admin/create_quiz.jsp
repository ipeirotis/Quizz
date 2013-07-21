<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Create new quiz" /></jsp:include>

<body>

	<div class="container">

		<form class="form-horizontal" id="createQuiz"
			action="/addQuiz" method="post"
			style="background-color: #D4D4D4; border-radius: 5px; padding: 10px"
			>
			<fieldset>
				<legend style="text-align: center">Create a new quiz</legend>
				<div class="control-group">
					<label class="control-label" for="name">Name:</label>
					<div class="controls">
						<input class="input-xxlarge" id="name" name="name" type="text">
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="relation">Relation:</label>
					<div class="controls">
						<input class="input-xxlarge" id="relation" name="relation"
							type="text">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="text">Question Text:</label>
					<div class="controls">
						<input class="input-xxlarge" id="text" name="text" type="text">
					</div>
				</div>
				
				<!-- 
				<div class="control-group">
					<label class="control-label" for="fbtype">Freebase Type:</label>
					<div class="controls">
						<input class="input-xxlarge" id="fbtype" name="fbtype" type="text">
					</div>
				</div>
				 -->
				
				<div class="control-group">
					<label class="control-label" for="adheadline">Ad headline:</label>
					<div class="controls">
						<input class="input-xxlarge" id="adheadline" name="adheadline"
							type="text">
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="adline1">Ad line1:</label>
					<div class="controls">
						<input class="input-xxlarge" id="adline1" name="adline1"
							type="text">
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="adline2">Ad line2:</label>
					<div class="controls">
						<input class="input-xxlarge" id="adline2" name="adline2"
							type="text">
					</div>
				</div>



				<div class="control-group">
					<label class="control-label" for="keywords">Ad keywords:</label>
					<div class="controls">
						<input class="input-xxlarge" id="keywords" name="keywords"
							type="text">
					</div>
				</div>



				<div class="control-group">
					<label class="control-label" for="budget">Daily ad budget:</label>
					<div class="controls">
						<input class="input-xxlarge" id="budget" name="budget" type="text"
							>
					</div>
				</div>

				<div class="control-group">
					<label class="control-label" for="cpcbid">CPC bid:</label>
					<div class="controls">
						<input class="input-xxlarge" id="cpcbid" name="cpcbid" type="text"
							>
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