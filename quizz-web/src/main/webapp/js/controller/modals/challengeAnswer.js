angular.module('quizz').controller('ChallengeAnswerController',
  ['$scope', '$routeParams', 'questionService', 'workflowService',
   function ($scope, $routeParams, questionService, workflowService) {

  $scope.send = function() {
    var params = {
        quizID: $routeParams.quizId,
        questionID: workflowService.getCurrentQuestion().id,
        userAnswerID: workflowService.getLastAnswer().key.id,
        userid: workflowService.getLastFeedback().userid,
        message: $scope.challengeAnswerMessage
    };
    questionService.challengeAnswer(
      params,
      function(response) {
        $scope.hide();
      },
      function(error) {
      });
  };
}]);
