angular.module('quizz-admin').controller('ScoreByDomainReportController', 
	['$scope', 'reportService', function($scope, reportService) {
		
	$scope.load = function(){
		reportService.loadScoreByDomainReport(
			function(response) {
				$scope.reportData = response.items;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.load();
		
}]);