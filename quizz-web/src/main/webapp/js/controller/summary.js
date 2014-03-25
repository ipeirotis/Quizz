angular.module('quizz').controller('SummaryController',
    ['$scope', '$routeParams', '$location', 'workflowService',
         function ($scope, $routeParams, $location, workflowService) {
  $scope.numOfQuestions = workflowService.getNumOfQuestions();
  $scope.correctAnswersCount = workflowService.getNumOfCorrectAnswers();

  $scope.startAgain = function() {
    workflowService.clear();
    $location.path('/quiz');
  };
}]);
