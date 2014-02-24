angular.module('quizz-admin', ['ngRoute', 'ngSanitize'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/quizzes', {templateUrl: templates.quizzes, controller: 'QuizzesController'})
	.when('/reports/answers', {templateUrl: templates.answersReport, controller: 'AnswersReportController', reloadOnSearch:false})
	.when('/reports/scoreByBrowser', {templateUrl: templates.scoreByBrowserReport, controller: 'ScoreByBrowserReportController', reloadOnSearch:false})
	.when('/reports/scoreByDomain', {templateUrl: templates.scoreByDomainReport, controller: 'ScoreByDomainReportController', reloadOnSearch:false})
	.when('/reports/contributionQuality', {templateUrl: templates.contributionQualityReport, controller: 'ContributionQualityReportController', reloadOnSearch:false})
	.otherwise({redirectTo: '/report'});
}])

.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push('interceptor');
}]);