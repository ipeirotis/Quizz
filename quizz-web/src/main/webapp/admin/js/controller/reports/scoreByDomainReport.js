angular.module('quizz-admin').controller('ScoreByDomainReportController', 
	['$scope', 'reportService', function($scope, reportService) {
	
	$scope.pageNumber = 0;
	
	$scope.load = function(){
		reportService.loadScoreByDomainReport($scope.pageNumber,
			function(response) {
				$scope.reportData = response;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.load();
	
	$scope.nextPage = function(){
		$scope.pageNumber++;
		$scope.load();
	};
	
	$scope.prevPage = function(){
		if($scope.pageNumber > 0){
			$scope.pageNumber--;
			$scope.load();
		}
	};
		
}]);