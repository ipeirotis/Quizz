angular.module('quizz').controller('FeedbackController',
    ['$scope', '$routeParams', '$location', '$q', '$modal',
     'questionService', 'workflowService', 'templates',
     function ($scope, $routeParams, $location, $q, $modal,
               questionService, workflowService, templates) {
  $scope.feedback = workflowService.getLastFeedback();
  $scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
  $scope.numOfQuestions = workflowService.getNumOfQuestions();

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
        workflowService.getNumOfQuestions()) {
      $location.path('/quiz');
    } else {
      $location.path('/summary');
    }
  };

  $scope.getFeedbackMessage = function() {
    if (workflowService.getIsCurrentQuestionGold()) {
      return 'The correct answer is ';
    } else {
      return 'More users say the answer is ';
    }
  };

  $scope.feedbackMessage = $scope.getFeedbackMessage();
}]);
