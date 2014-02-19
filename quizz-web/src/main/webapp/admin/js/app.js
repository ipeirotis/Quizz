angular.module('quizz-admin', ['ngRoute', 'ngSanitize'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/quizzes', {templateUrl: templates.quizzes, controller: 'QuizzesController'})
	.when('/report', {templateUrl: templates.report, controller: 'ReportController', reloadOnSearch:false})
	.otherwise({redirectTo: '/report'});
}])

.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push('interceptor');
}]);