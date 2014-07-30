angular.module('quizz').controller('EditorListQuestionsController',
    ['$scope', '$routeParams', '$sce', 'editorService', function ($scope, $routeParams, $sce, editorService) {

      $scope.quizID = $routeParams.quizID;

      $scope.loadQuestions = function () {
        editorService.listQuestions($routeParams.quizID, function(response) {
          $scope.questions = response.items;
          $scope.readyToShow = true;
        },
        function(error) {
          console.log(error);
        });
      };

      $scope.loadQuestions();

      $scope.trustAsHtml = function(html_code) {
        return $sce.trustAsHtml(html_code);
      };

      $scope.removeQuestion = function(id) {
        editorService.removeQuestion(id, function(response) {
          $scope.loadQuestions();
        },
        function(error) {
          console.log(error);
        });
      };
}]);
