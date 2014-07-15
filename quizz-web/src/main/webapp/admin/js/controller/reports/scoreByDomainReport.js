angular.module('quizz-admin').controller('ScoreByDomainReportController',
  ['$scope', 'reportService', function($scope, reportService) {

  $scope.pageNumber = 0;

  $scope.load = function() {
    reportService.loadScoreByDomainReport($scope.pageNumber,
      function(response, isCallback) {
        $scope.reportData = response;
        $scope.readyToShow = true;
        // TODO(chunhowt): loadScoreByDomainReport might call success
        // callback directly instead of in the callback if the data is
        // cached locally, and thus, we need to check that before calling
        // $apply.
        if (isCallback) {
          $scope.$digest();
        }
      },
      function(error) {
      });
  };

  $scope.load();

  $scope.nextPage = function() {
    $scope.pageNumber++;
    $scope.load();
  };

  $scope.prevPage = function() {
    if ($scope.pageNumber > 0) {
      $scope.pageNumber--;
      $scope.load();
    }
  };
}]);
