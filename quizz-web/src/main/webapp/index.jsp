<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html ng-app="quizz">
<head>
<title>Quizz</title>

<meta name="description" content="">
<meta name="viewport" content="width=device-width,initial-scale=1.0">

<link rel="stylesheet" type="text/css" href="/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/bootstrap-custom.css">
<link rel="stylesheet" type="text/css" href="/css/style.css?v=20140204203136">

<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
   	
<script	src="/lib/jquery.min.js" type="text/javascript"></script>
<script	src="/lib/jquery.cookie.js" type="text/javascript"></script>
<script	src="/lib/angular.js" type="text/javascript"></script>
<script	src="/lib/angular-resource.js" type="text/javascript"></script>
<script	src="/lib/angular-route.js" type="text/javascript"></script>
<script	src="/lib/angular-sanitize.js" type="text/javascript"></script>
<script src="/lib/bootstrap.js" type="text/javascript"></script>
<script src="/js/all.js?v=20140204203136" type="text/javascript"></script>

<script>
	var Config = {
		api : 'http://localhost:8888/_ah/api/quizz/v1'
	};
</script>

</head>

<body>
<div class="container">
	<div class="row">
		<div class="col-sm-offset-3 col-sm-6 col-md-offset-3 col-md-6 col-lg-offset-3 col-lg-6">
			<div class="text-center">
				<a href="/">
					<img src="assets/logo.png" width="320px" />
				</a>
			</div>
			<div id="content" ng-view></div>
        </div>
	</div>
</div>

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
	})(window, document, 'script', '//www.google-analytics.com/analytics.js',
			'ga');

	ga('create', 'UA-42553914-1', 'quizz.us');
	ga('send', 'pageview');
</script>
</body>
</html>
