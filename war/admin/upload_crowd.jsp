<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Quiz"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.QuizQuestion"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.FreebaseSearch"%>
<%@ page import="java.util.List"%>
<%@ page
	import="com.google.appengine.api.blobstore.BlobstoreServiceFactory"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>List of Supported Relations</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="/bootstrap/css/bootstrap.min.css" rel="stylesheet"
	media="screen">
<script src="/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>


<link type="text/css" rel="stylesheet" href="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.css" />
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
<script type="text/javascript" src="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.js"></script>
<script src="http://malsup.github.com/jquery.form.js"></script>
<script src="http://code.jquery.com/jquery-migrate-1.2.1.js"></script>
<link type="text/css" rel="stylesheet" href="/qtip/jquery.qtip.css" />
<script type="text/javascript" src="/qtip/jquery.qtip.js"></script>


</head>
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
							<input id="answers_file" name="answers_file" type="file"> 
						</span>
						<button type="submit" class="btn">Submit</button>	
					</div>
				</div>
			</fieldset>
		</form>
	</div>

	<!-- Setup help tooltips for the different page elements -->
	<script>
	$('#answers_file').qtip({content: 'A file that contains previously submitted user answers for the quiz. The format is the same as the one downloaded from the admin interface through the download data function.'});
	</script>

	<script>
		$(document).ready(function() {
			// bind 'myForm' and provide a simple callback function 
			$('#uploadCrowdAnswers').ajaxForm(function() {
				alert("Previous user answers added!");
				window.location.replace("/admin/");
			});
		});
	</script>

	<script>
		(function(i, s, o, g, r, a, m) {
			i['GoogleAnalyticsObject'] = r;
			i[r] = i[r] || function() {
				(i[r].q = i[r].q || []).push(arguments)
			}, i[r].l = 1 * new Date();
			a = s.createElement(o), m = s.getElementsByTagName(o)[0];
			a.async = 1;
			a.src = g;
			m.parentNode.insertBefore(a, m)
		})(window, document, 'script',
				'//www.google-analytics.com/analytics.js', 'ga');

		ga('create', 'UA-89122-22', 'crowd-power.appspot.com');
		ga('send', 'pageview');
	</script>

</body>
</html>