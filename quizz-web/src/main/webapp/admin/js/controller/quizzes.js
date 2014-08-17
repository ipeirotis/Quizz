angular.module('quizz-admin').controller('QuizzesController',
    ['$scope', 'reportService', 'campaignService', function ($scope, reportService, campaignService) {

      $scope.loadQuizes = function() {
        reportService.listQuizes(
          function(response) {
            $scope.quizzes = response.items;
            $scope.readyToShow = true;
          },
          function(error) {
          });
      };

      $scope.loadQuizes();

}]);
