<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<%@ page import="com.google.appengine.api.utils.SystemProperty"%>

<%
  boolean isDev = SystemProperty.environment.value() != SystemProperty.Environment.Value.Production;
  String apiUrl;

  if (isDev)
    apiUrl = "http://" + request.getServerName() + ":" + request.getServerPort();
  else
    apiUrl = "https://" + SystemProperty.applicationId.get() + ".appspot.com";
%>

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

<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="css/bootstrap-custom.css">
<link rel="stylesheet" type="text/css" href="css/style.css">

<!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-resource.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-route.js" type="text/javascript"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-sanitize.js" type="text/javascript"></script>
<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.js" type="text/javascript"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/angular-ui-bootstrap/0.10.0/ui-bootstrap-tpls.min.js" type="text/javascript"></script>
<% if (isDev) {%>
<script src="js/app.js" type="text/javascript"></script>
<script src="js/templates.js" type="text/javascript"></script>
<script src="js/controller/reports/contributionQualityReport.js" type="text/javascript"></script>
<script src="js/controller/reports/multiChoiceAnswersReport.js" type="text/javascript"></script>
<script src="js/controller/reports/scoreByBrowserReport.js" type="text/javascript"></script>
<script src="js/controller/reports/scoreByDomainReport.js" type="text/javascript"></script>
<script src="js/controller/menu.js" type="text/javascript"></script>
<script src="js/controller/quizzes.js" type="text/javascript"></script>
<script src="js/controller/quiz.js" type="text/javascript"></script>
<script src="js/controller/modals/campaignModalController.js" type="text/javascript"></script>
<script src="js/directive/reports/answersHeaderRow.js" type="text/javascript"></script>
<script src="js/directive/reports/answersRow.js" type="text/javascript"></script>
<script src="js/directive/navbar.js" type="text/javascript"></script>
<script src="js/service/interceptor.js" type="text/javascript"></script>
<script src="js/service/loading.js" type="text/javascript"></script>
<script src="js/service/reportService.js" type="text/javascript"></script>
<script src="js/service/quizService.js" type="text/javascript"></script>
<script src="js/service/campaignService.js" type="text/javascript"></script>
<% } else {%>
<script src="js/all.js" type="text/javascript"></script>
<% } %>
<script src='/_ah/channel/jsapi'></script>

</head>

<script>
  var SCOPES = 'https://www.googleapis.com/auth/userinfo.email';
  var CLIENT_ID = '<%=System.getProperty("AUTH_CLIENT_ID")%>';

  var Config = {
    api : '<%=apiUrl%>/_ah/api/quizz/v1'
  };

  // Attempts to authorize the app to access the secured API.
  // Params:
  //   mode: A boolean to indicate the value of the "immediate" field for the
  //         authorize function's param. If true, the authorization will happen
  //         in background using existing authentication token, if any. If there
  //         is no authentication token available, it will fail to authorize.
  //         If false, a window will pop up to prompt user to explicitly
  //         authorize the app.
  function signIn(mode) {
    var params = {};
    params.immediate = mode;
    params.client_id = CLIENT_ID;
    params.scope = SCOPES;

    gapi.auth.authorize(params, function() {
      gapi.client.oauth2.userinfo.get().execute(function(resp) {
        if (!resp.code) {
          var elements = document.getElementsByClassName('loginButton');
          for (var i = 0; i < elements.length; ++i) {
            elements[i].style.visibility = 'hidden';
          }
        }
      });
    });
  };

  // Callback function to initialize the endpoints javascript library upon
  // page loading and set up the handlers for the login buttons..
  function initEndpoint() {
    var apiRoot = '<%=apiUrl%>/_ah/api';
    var apisToLoad = 2;
    var loadCallback = function() {
      if (--apisToLoad == 0) {
        signIn(true);
      }
    };

    gapi.client.load('quizz', 'v1', loadCallback, apiRoot);
    gapi.client.load('oauth2', 'v2', loadCallback);

    var elements = document.getElementsByClassName('loginButton');
    for (var i = 0; i < elements.length; ++i) {
      elements[i].onclick = function() {signIn(false)};
    }
  };
</script>

<script src="https://apis.google.com/js/client.js?onload=initEndpoint"></script>

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
        <li data-match-route="/quizzes/([A-Za-z0-9]+)"><a href="#/quizzes">Quizzes</a></li>
        <li data-match-route="/reports/([A-Za-z0-9]+)" class="dropdown">
          <a href="" class="dropdown-toggle"  data-toggle="dropdown">Reports <b class="caret"></b></a>
          <ul class="dropdown-menu">
            <li><a href="#/reports/multiChoiceAnswers">Multi choice answers</a></li>
            <li><a href="#/reports/freeTextAnswers">Free text answers</a></li>
            <li><a href="#/reports/scoreByBrowser">Browsers</a></li>
            <li><a href="#/reports/scoreByDomain">Domains</a></li>
            <li><a href="#/reports/contributionQuality">Contribution quality</a></li>
          </ul>
        </li>
      </ul>
      <div style="padding: 8px; text-align: right">
        <input class="loginButton" type="button" value="Authenticate"/>
      </div>
    </div>
  </nav>
  <div id="content" ng-view></div>
</body>
</html>
