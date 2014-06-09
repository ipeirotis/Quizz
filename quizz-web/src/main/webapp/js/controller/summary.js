angular.module('quizz').controller('SummaryController',
    ['$scope', '$routeParams', '$location', 'workflowService',
         function ($scope, $routeParams, $location, workflowService) {
  $scope.numQuestions = workflowService.getNumQuestions();
  $scope.numAttemptedQuestions = workflowService.getNumSubmittedUserAnswers();
  $scope.correctAnswersCount = workflowService.getNumCorrectAnswers();

  $scope.startAgain = function() {
    workflowService.clear();
    $location.path('/quiz');
  };
}]);
