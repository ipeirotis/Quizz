angular.module('quizz', ['ngRoute', 'ngSanitize', 'ui.bootstrap', 'summernote'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {
  $routeProvider
      .when('/feedback', {
                templateUrl: templates.feedback,
                controller: 'FeedbackController'
            })
      .when('/list', {
                templateUrl: templates.list,
                controller: 'ListController'
            })
      .when('/quiz', {
                templateUrl: templates.quiz,
                controller: 'QuizController',
                reloadOnSearch:false
            })
      .when('/summary', {
                templateUrl: templates.summary,
                controller: 'SummaryController'
            })
      .when('/editor/quiz/list', {
                templateUrl: templates.editorListQuizzes,
                controller: 'EditorListQuizzesController'
            })
      .when('/editor/quiz/new', {
                templateUrl: templates.editorEditQuiz,
                controller: 'EditorEditQuizController'
            })
      .when('/editor/quiz/:quizID/edit', {
                templateUrl: templates.editorEditQuiz,
                controller: 'EditorEditQuizController'
            })
      .when('/editor/quiz/:quizID/question/list', {
                templateUrl: templates.editorListQuestions,
                controller: 'EditorListQuestionsController'
            })
      .when('/editor/quiz/:quizID/question/new', {
                templateUrl: templates.editorEditQuestion,
                controller: 'EditorEditQuestionController'
            })
      .when('/editor/quiz/:quizID/question/:questionID/edit', {
                templateUrl: templates.editorEditQuestion,
                controller: 'EditorEditQuestionController'
            })
      .otherwise({redirectTo: '/list'});
}])

.config(['$httpProvider', function($httpProvider) {
  $httpProvider.interceptors.push('interceptor');
}])

.run(['$rootScope', 'utils',
    function($rootScope, utils) {
      $rootScope.utils = utils;
    }]);
