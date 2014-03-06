<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.google.appengine.api.utils.SystemProperty"%>

<!DOCTYPE html>
<html ng-app="quizz">
<head>
<title>Quizz.us</title>

<meta name="description" content="">
<meta name="viewport" content="width=device-width,initial-scale=1.0">

<link rel="shortcut icon" type="image/x-icon" href="/assets/favicon.ico">

<link rel="apple-touch-icon-precomposed" sizes="144x144"
	href="/assets/144x144.png">
<link rel="apple-touch-icon-precomposed" sizes="114x114"
	href="/assets/114x114.png">
<link rel="apple-touch-icon-precomposed" sizes="72x72"
	href="/assets/72x72.png">
<link rel="apple-touch-icon-precomposed" href="/assets/57x57.png">


<meta property="og:title"
	content="Quizz.us: Test yourself, Compare yourself, Learn new things" />
<meta property="og:type" content="Quizzes" />
<meta property="og:url" content="http://www.quizz.us" />
<meta property="og:image"
	content="http://www.quizz.us/assets/144x144.png" />
<meta property="og:description"
	content="A set of quizzes that will assess your knowledge in various areas, and allow you to compare against others." />
<meta property="og:site_name" content="Quizz.us" />

<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/bootstrap-custom.css">
<link rel="stylesheet" type="text/css" href="/css/style.css">

<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js" type="text/javascript"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.3.1/jquery.cookie.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-resource.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-route.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-sanitize.js" type="text/javascript"></script>
<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.js" type="text/javascript"></script>
<script src="/js/all.js" type="text/javascript"></script>
<script src='/_ah/channel/jsapi'></script>

<!-- g+ sharing. -->
<script type="text/javascript"
	src="https://apis.google.com/js/plusone.js">
  {parsetags: 'explicit'}
</script>
</head>
<%
	boolean isDev = SystemProperty.environment.value() != SystemProperty.Environment.Value.Production;
	String apiUrl;
	String serverName = request.getServerName();
	if (isDev)
		apiUrl = "http://" + serverName + ":" + request.getServerPort();
	else {

		if (serverName.startsWith("www.quizz.us")
				|| serverName.startsWith("quizz.us"))
			apiUrl = "https://crowd-power.appspot.com";
		else
			apiUrl = "https://" + serverName;
	}
%>

<script>
	var Config = {
		api : '<%=apiUrl%>/_ah/api/quizz/v1'
	};
</script>

</head>

<body>
	<div id="wrap">
		<div class="container">
			<div class="row">
				<div class="col-sm-offset-3 col-sm-6 col-md-offset-3 col-md-6 col-lg-offset-3 col-lg-6">
					<div class="text-center">
						<a href="/"> <img src="assets/logo.png" width="320px" />
						</a>
					</div>
					<div id="content" ng-view></div>
				</div>
			</div>
		</div>
		<div id="push"></div>
	</div>

	<div id="footer">
		<div class="container">
			<div
				class="col-sm-offset-3 col-sm-6 col-md-offset-3 col-md-6 col-lg-offset-3 col-lg-6">
				<div class="text-center">
					<small> <a
						href="https://www.google.com/intl/en/policies/terms/">Terms
							of Service </a> - <a
						href="https://www.google.com/intl/en/policies/privacy/">Privacy
							Policy </a> - <a href="about.html">About</a>
					</small>
				</div>
			</div>
		</div>
	</div>



<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-42553914-1', 'quizz.us');
  ga('send', 'pageview');

</script>

</body>
</html>
