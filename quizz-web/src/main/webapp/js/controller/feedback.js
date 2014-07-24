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

    if (!workflowService.isQuizFinished()) {
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

  $scope.getPicksPercentage = function(internalID) {
    var answers = $scope.currentQuestion.answers;
    var current = 0;
    var total = 0;
    angular.forEach(answers, function(answer) {
      total += answer.numberOfPicks || 0;
      if (answer.internalID == internalID) {
        current = answer.numberOfPicks || 0;
      }
    });

    if (total < 5) {
      return '';
    } else {
      if (current == 0) {
        return '';
      } else {
        return '(' + (current * 100. / total).toFixed(0) +
            '% picked this answer)';
      }
    }
  };
}]);
