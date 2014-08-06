angular.module('quizz').controller('EditorEditQuizController',
    ['$scope', '$location', '$routeParams', 'editorService',
     function ($scope, $location, $routeParams, editorService) {

      $scope.quizID = $routeParams.quizID;

      $scope.quiz = {
        kind: 'MULTIPLE_CHOICE',
        questions: 0,
        showOnDefault: true
      };

      $scope.loadQuiz = function () {
        if($routeParams.quizID) {
          editorService.getQuiz($routeParams.quizID, function(response) {
            $scope.quiz = response;
            $scope.quizID = $scope.quiz.quizID;
            $scope.readyToShow = true;
          },
          function(error) {
            console.log(error);
          });
        } else {
          $scope.readyToShow = true;
        }
      };

      $scope.loadQuiz();

      $scope.saveQuiz = function (form) {
        if(form.$invalid){
          $scope.notValidForm = true;
          return;
        }

        editorService.saveQuiz($scope.quiz, function(response) {
          $scope.quizID = response.quizID;
          $scope.error = '';
          $scope.success = 'Quiz saved successfully';
        },
        function(error) {
          console.log(error);
          $scope.error = response.error.message;
        });
      };
}]);
