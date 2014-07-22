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
          $scope.readyToShow = true;
        }
      };
      
      $scope.loadQuestion();
      
      $scope.saveQuestion = function (form) {
        if(form.$invalid){
          $scope.notValidForm = true;
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
          $scope.question.answers.push(answer);
        });
      };
}]);
