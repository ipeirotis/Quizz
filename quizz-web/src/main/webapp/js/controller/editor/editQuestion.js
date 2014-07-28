angular.module('quizz').controller('EditorEditQuestionController',
    ['$scope', '$modal', '$location', '$routeParams', 'editorService', 'templates',
     function ($scope, $modal, $location, $routeParams, editorService, templates) {

      $scope.questionID = $routeParams.questionID;
      $scope.quizID = $routeParams.quizID;

      $scope.question = {
          quizID: $routeParams.quizID,
          kind: 'MULTIPLE_CHOICE_CALIBRATION',
          answers: []
      };

      $scope.loadQuestion = function () {
        if($routeParams.questionID) {
          editorService.getQuestion($routeParams.questionID, function(response) {
            $scope.question = response;
            $scope.readyToShow = true;
          },
          function(error) {
            console.log(error);
          });
        } else {
          editorService.getQuiz($routeParams.quizID, function(quiz) {
            if(quiz.kind == 'FREE_TEXT') {
              $scope.question.kind = 'FREETEXT_CALIBRATION';
            } else if(quiz.kind == 'MULTIPLE_CHOICE') {
              $scope.question.kind = 'MULTIPLE_CHOICE_CALIBRATION';
            }
            $scope.readyToShow = true;
          },
          function(error) {
            console.log(error);
          });
        }
      };
      
      $scope.loadQuestion();
      
      $scope.saveQuestion = function (form) {
        if(form.$invalid){
          $scope.notValidForm = true;
          return;
        }
        
        var hasGoldAnswer = false;
        angular.forEach($scope.question.answers, function(answer) {
          if (answer.kind == 'GOLD') {
            hasGoldAnswer = true;
          }
        });
        
        if($scope.question.answers && $scope.question.answers.length > 0 && hasGoldAnswer == false) {
          $scope.error = 'At least one gold answer is required';
          return;
        }
        
        editorService.saveQuestion($scope.question, function(response) {
          $scope.error = '';
          $scope.success = 'Question saved successfully';
        },
        function(response) {
          $scope.error = response.error.message;
        });
      };
      
      $scope.showAnswerModal = function(answer) {
        var modalInstance = $modal.open({
          templateUrl: templates.editorAnswer,
          controller: 'AnswerModalController',
          resolve: {
            answer: function () {
              return answer;
            }
          }
        });

        modalInstance.result.then(function (answer) {
          if(!$scope.question.answers) {
            $scope.question.answers = [];
          }

          if(answer.internalID === undefined) {
            answer.internalID = $scope.question.answers.length + 1;
            $scope.question.answers.push(answer);
          }
        });
      };

      $scope.deleteAnswer = function(index) {
        $scope.question.answers.splice(index, 1);
      };
}]);
