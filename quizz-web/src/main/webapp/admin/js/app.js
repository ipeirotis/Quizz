angular.module('quizz-admin', ['ngRoute', 'ngSanitize', 'ui.bootstrap'])
    .config(['$routeProvider', 'templates',
             function($routeProvider, templates) {  
               $routeProvider
                   .when('/quizzes', {
                             templateUrl: templates.quizzes,
                             controller: 'QuizzesController'
                         })
                   .when('/quizzes/:quizID', {
                             templateUrl: templates.quiz,
                             controller: 'QuizController'
                         })
                   .when('/reports/multiChoiceAnswers', {
                             templateUrl: templates.multiChoiceAnswersReport,
                             controller: 'MultiChoiceAnswersReportController',
                             reloadOnSearch: false
                         })
                   .when('/reports/scoreByBrowser', {
                             templateUrl: templates.scoreByBrowserReport,
                             controller: 'ScoreByBrowserReportController',
                             reloadOnSearch: false
                         })
                   .when('/reports/scoreByDomain', {
                             templateUrl: templates.scoreByDomainReport,
                             controller: 'ScoreByDomainReportController',
                             reloadOnSearch: false
                         })
                   .when('/reports/contributionQuality', {
                             templateUrl: templates.contributionQualityReport,
                             controller: 'ContributionQualityReportController',
                             reloadOnSearch: false
                         })
                   .otherwise({redirectTo: '/quizzes'});
             }])

.config(['$httpProvider', function($httpProvider) {
  $httpProvider.interceptors.push('interceptor');
}]);
