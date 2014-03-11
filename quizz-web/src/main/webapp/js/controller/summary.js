angular.module('quizz').controller('SummaryController',
    ['$scope', '$routeParams', '$location', 'workflowService',
         function ($scope, $routeParams, $location, workflowService) {
  $scope.numOfQuestions = workflowService.getNumOfQuestions();

  $scope.getCorrectAnswersCount = function() {
    var count = 0;
    angular.forEach(workflowService.getUserFeedbacks(), function(feedback) {
      if (feedback.isCorrect)
        count++;
    });
    return count;
  };

  $scope.startAgain = function() {
    workflowService.clear();
    $location.path('/quiz');
  };
}]);
