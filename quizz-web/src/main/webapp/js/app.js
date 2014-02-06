angular.module('quizz', ['ngRoute', 'ngSanitize', 'ezfb'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/feedback', {templateUrl: templates.feedback, controller: 'FeedbackController'})
	.when('/list', {templateUrl: templates.list, controller: 'ListController'})
	.when('/quiz', {templateUrl: templates.quiz, controller: 'QuizController', reloadOnSearch:false})
	.when('/summary', {templateUrl: templates.summary, controller: 'SummaryController'})
	.otherwise({redirectTo: '/list'});
}])

.config(['$httpProvider', '$FBProvider', function($httpProvider, $FBProvider) {
	$httpProvider.interceptors.push('interceptor');
	
	$FBProvider.setInitParams({
	    appId: '220743704753581'
	});  
}])

.run(['$rootScope', 'utils', 
    function($rootScope, utils) {
	$rootScope.utils = utils;
}]);