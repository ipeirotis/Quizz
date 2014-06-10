angular.module('quizz').controller('ChallengeAnswerController',
  ['$scope', '$routeParams', 'questionService', 'workflowService',
   function ($scope, $routeParams, questionService, workflowService) {
  $scope.reason = 'OTHER';
  $scope.showOptions = true;
  $scope.showThanks = false;

  $scope.send = function() {
    questionService.challengeAnswer(
      $routeParams.quizId,
      workflowService.getCurrentQuestion().id,
      workflowService.getLastAnswer().id,
      workflowService.getLastFeedback().userid,
      $scope.challengeAnswerMessage,
      $scope.challengeCorrectValueMessage,
      $scope.challengeURLMessage,
      $scope.reason,
      function(response) {
        $scope.hide();
      },
      function(error) {
      });
  };

  $scope.saveOption = function(option) {
    $scope.reason = option;
    $scope.showOptions = false;
    $scope.showThanks = true;

    // We have the reason code, so we should send it over first in case the user
    // closes the window, quit etc instead of pressing send in the 2nd window.
    questionService.challengeAnswer(
      $routeParams.quizId,
      workflowService.getCurrentQuestion().id,
      workflowService.getLastAnswer().id,
      workflowService.getLastFeedback().userid,
      $scope.challengeAnswerMessage,
      $scope.challengeCorrectValueMessage,
      $scope.challengeURLMessage,
      $scope.reason,
      function(response) {},
      function(error) {
      });
  };
}]);
