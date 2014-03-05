<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.google.appengine.api.utils.SystemProperty"%>

<!DOCTYPE html>
<html ng-app="quizz-admin">
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

<link rel="stylesheet" type="text/css" href="/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="css/bootstrap-custom.css?v=20140304175722">
<link rel="stylesheet" type="text/css" href="css/style.css?v=20140304175722">

<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->

<script src="/lib/jquery.min.js" type="text/javascript"></script>
<script src="/lib/angular.js" type="text/javascript"></script>
<script src="/lib/angular-resource.js" type="text/javascript"></script>
<script src="/lib/angular-route.js" type="text/javascript"></script>
<script src="/lib/angular-sanitize.js" type="text/javascript"></script>
<script src="/lib/bootstrap.js" type="text/javascript"></script>
<script src="js/all.js?v=20140304175722" type="text/javascript"></script>
<script src='/_ah/channel/jsapi'></script>

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
	<nav class="navbar navbar-default" role="navigation" ng-controller="MenuController" navbar>
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
		</div>
		<div class="collapse navbar-collapse">
			<ul class="nav navbar-nav">
				<li data-match-route="/quizzes"><a href="#/quizzes">Quizzes</a></li>
				<li data-match-route="/reports/([A-Za-z0-9]+)" class="dropdown">
					<a href="" class="dropdown-toggle"	data-toggle="dropdown">Reports <b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li><a href="#/reports/multiChoiceAnswers">Multi choice answers</a></li>
						<li><a href="#/reports/freeTextAnswers">Free text answers</a></li>
						<li><a href="#/reports/scoreByBrowser">Browsers</a></li>
						<li><a href="#/reports/scoreByDomain">Domains</a></li>
						<li><a href="#/reports/contributionQuality">Contribution quality</a></li>
					</ul>
				</li>
			</ul>
		</div>
	</nav>
	<div id="content" ng-view></div>
</body>
</html>