angular.module('quizz').controller('ChallengeAnswerController',
  ['$scope', '$routeParams', 'questionService', 'workflowService',
   function ($scope, $routeParams, questionService, workflowService) {

  $scope.send = function() {
    questionService.challengeAnswer(
      $routeParams.quizId,
      workflowService.getCurrentQuestion().id,
      workflowService.getLastAnswer().id,
      workflowService.getLastFeedback().userid,
      $scope.challengeAnswerMessage,
      function(response) {
        $scope.hide();
      },
      function(error) {
      });
  };
}]);
