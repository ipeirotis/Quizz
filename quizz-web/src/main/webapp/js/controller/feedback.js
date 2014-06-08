angular.module('quizz').controller('FeedbackController',
    ['$scope', '$routeParams', '$location', '$q', '$modal',
     'questionService', 'workflowService', 'templates',
     function ($scope, $routeParams, $location, $q, $modal,
               questionService, workflowService, templates) {
  $scope.feedback = workflowService.getLastFeedback();
  $scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
  $scope.numQuestions = workflowService.getNumQuestions();
  $scope.currentQuestion = workflowService.getCurrentQuestion();
  $scope.bestAnswerID = workflowService.getBestAnswerId();
  $scope.userAnswerID = workflowService.getUserAnswerId();

  $scope.challengeAnswer = function() {
    var modalPromise = $modal({
        template: templates.challengeAnswer,
        persist: false,
        show: false,
        keyboard: true,
        data: {}
    });

    $q.when(modalPromise).then(function(modalEl) {
        modalEl.modal('show');
    });
  };

  $scope.nextQuestion = function() {
    workflowService.incCurrentQuestionIndex();

    if (workflowService.getCurrentQuestionIndex() <
        workflowService.getNumQuestions()) {
      $location.path('/quiz');
    } else {
      $location.path('/summary');
    }
  };

  $scope.filterSelectable = function(answer) {
    return answer.kind == 'GOLD' ||
           answer.kind == 'INCORRECT' ||
           answer.kind == 'SILVER';
  };
}]);
