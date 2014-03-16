angular.module('quizz-admin').controller('ScoreByBrowserReportController',
  ['$scope', 'reportService', function($scope, reportService) {

  $scope.load = function() {
    reportService.loadScoreByBrowserReport(
      function(response) {
        $scope.reportData = response.items;
        $scope.readyToShow = true;
      },
      function(error) {
      });
  };

  $scope.load();
}]);
