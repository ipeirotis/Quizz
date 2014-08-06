angular.module('quizz').controller('EditorListQuizzesController',
    ['$scope', '$rootScope', 'editorService',
     function ($scope, $rootScope, editorService) {

      $scope.loadQuizzes = function () {
        editorService.listQuizzes(function(response) {
          $scope.quizzes = response;
          $scope.readyToShow = true;
        },
        function(error) {
          console.log(error);
        });
      };

      $scope.loadQuizzes();
}]);
