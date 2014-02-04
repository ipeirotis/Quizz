angular.module('quizz', ['ngRoute'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/feedback', {templateUrl: templates.feedback, controller: 'FeedbackController'})
	.when('/list', {templateUrl: templates.list, controller: 'ListController'})
	.when('/quiz', {templateUrl: templates.quiz, controller: 'QuizController', reloadOnSearch:false})
	.when('/summary', {templateUrl: templates.summary, controller: 'SummaryController'})
	.otherwise({redirectTo: '/list'});
}])

.config(['$httpProvider', function($httpProvider) {
	$httpProvider.responseInterceptors.push('interceptor');
}])

.run(['$rootScope', 'utils', 
    function($rootScope, utils) {
	$rootScope.utils = utils;
}]);