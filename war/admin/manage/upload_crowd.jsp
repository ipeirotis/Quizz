<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService"%>

<jsp:include page="/header.jsp"><jsp:param name="title" value="Upload existing user answers" /></jsp:include>

<body>

	<%
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		String relation = request.getParameter("relation");
	%>

	<div class="container">
		<form class="form-horizontal" id="uploadCrowdAnswers"
			action="<%=blobstoreService.createUploadUrl("/uploadCrowdAnswers")%>"
			style="background-color: #D4D4D4; border-radius: 5px; padding: 10px"
			method="post" enctype="multipart/form-data">
			<fieldset>
				<legend style="text-align: center">Upload existing user answers to the quiz</legend>
				<input id="relation" name="relation" type="hidden" value="<%= relation %>">
				<div class="control-group">		
					<label class="control-label" for="answers_file">File:</label>
					<div class="controls">
						<span class="btn btn-file">
							<input id="answers_file" title="A file that contains previously submitted user answers for the quiz. The format is the same as the one downloaded from the admin interface through the download data function." name="answers_file" type="file"> 
						</span>
						<button type="submit" class="btn">Submit</button>	
					</div>
				</div>
			</fieldset>
		</form>
	</div>

</body>
</html>