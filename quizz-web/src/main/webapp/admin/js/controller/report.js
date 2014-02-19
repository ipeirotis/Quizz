angular.module('quizz-admin').controller('ReportController', 
	['$scope', '$rootScope', '$routeParams', '$location', 'reportService',
	 function ($scope, $rootScope, $routeParams, $location, reportService) {
		
	$scope.quizID = $routeParams.quizId;
	
	$scope.$watch('quizID', function(newValue, oldValue) {console.log(newValue)
		if(newValue && newValue != ''){
			$scope.load(newValue);
			$location.search('quizId', newValue);
			$routeParams.quizId = newValue;
		}else{
			$scope.reportData = [];
			$location.search('quizId', null);
			$routeParams.quizId = null;
		}
	});
	
	$scope.loadQuizes = function(){
		reportService.listQuizes(
			function(response) {
				$scope.quizes = response.items;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.loadQuizes();
	
	$scope.load = function(quizID){
		reportService.load($scope.quizID,
			function(response) {
				$scope.reportData = response.items;
			},
			function(error) {
		});
	};
	
}]);