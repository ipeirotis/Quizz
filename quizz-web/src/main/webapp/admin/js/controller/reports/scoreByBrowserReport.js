angular.module('quizz-admin').controller('ScoreByBrowserReportController',
  ['$scope', 'reportService', function($scope, reportService) {

  $scope.load = function() {
    reportService.loadScoreByBrowserReport(
      function(response) {
        $scope.$apply(function() {
            $scope.reportData = response.items;
            $scope.readyToShow = true;
        });
      },
      function(error) {
      });
  };

  $scope.load();
}]);
