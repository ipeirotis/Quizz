<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="com.ipeirotis.crowdquiz.utils.PMF"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.Question"%>
<%@ page import="com.ipeirotis.crowdquiz.entities.EntityQuestion"%>
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


<link type="text/css" rel="stylesheet"
	href="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.css" />
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
<script type="text/javascript"
	src="https://www.gstatic.com/freebase/suggest/4_1/suggest.min.js"></script>
<script src="http://malsup.github.com/jquery.form.js"></script>

</head>
<body>

	<%
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	%>


	<div class="container">

		<form class="form-horizontal" id="addRelation"
			action="<%=blobstoreService.createUploadUrl("/addRelation")%>"
			style="background-color: #D4D4D4; border-radius: 5px; padding:10px" method="post"
			enctype="multipart/form-data">
			<fieldset>
				<legend style="text-align: center">Add relation to crowdsource</legend>
								<div class="control-group">
					<label class="control-label" for="relation">Name:</label>
					<div class="controls">
						<input class="input-xxlarge" id="name" name="name"
							type="text" placeholder="Calories in food">
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="relation">Relation:</label>
					<div class="controls">
						<input class="input-xxlarge" id="relation" name="relation"
							type="text" placeholder="kc:/food/food:energy">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="text">Question Text:</label>
					<div class="controls">
						<input class="input-xxlarge" id="text" name="text" type="text"
							placeholder="How many calories (1 cal = 4.2 KJ) in 100 grams (3.5 oz) of">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="fbattr">FB Attribute:</label>
					<div class="controls">
						<input class="input-xxlarge" id="fbattr" name="fbattr" type="text"
							placeholder="/food/food/energy">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="fbtype">FB Type:</label>
					<div class="controls">
						<input class="input-xxlarge" id="fbtype" name="fbtype" type="text"
							placeholder="/type/float">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="myFile">File:<br><small><em>(A file from completions server [<a href="http://0.completion_server.completion-server.vc.borg.google.com:26062/attribute?locale=&q=kc:/food/food:energy">example</a>])</em></small> </label>
					<div class="controls">
						<span class="btn btn-file"><input id="myFile" name="myFile"
							type="file"> </span>
					</div>
				</div>

				<div class="form-actions"
					style="background-color: #D0D0D0; border-radius: 5px;">
					<button type="submit" class="btn">Submit</button>
				</div>
			</fieldset>
		</form>
	</div>



	<script>
		$(document).ready(function() {
			// bind 'myForm' and provide a simple callback function 
			$('#addRelation').ajaxForm(function() {
				alert("Relation added!");
				window.location.replace("/admin/");
			});
		});
	</script>

<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-89122-22', 'crowd-power.appspot.com');
  ga('send', 'pageview');

</script>

</body>
</html>