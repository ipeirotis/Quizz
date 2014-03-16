angular.module('quizz-admin').controller('ContributionQualityReportController',
  ['$scope', 'reportService', function ($scope, reportService) {

  $scope.load = function() {
    reportService.loadContributionQualityReport(
        function(response) {
          $scope.reportData = response.items;
          $scope.readyToShow = true;
        },
        function(error) {
        });
  };

  $scope.load();

  $scope.toPercentage = function(value) {
    return (100. * value).toFixed(0) + "%";
  };
}]);
