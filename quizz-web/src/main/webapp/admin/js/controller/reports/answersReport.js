angular.module('quizz-admin').controller('AnswersReportController', 
	['$scope', '$rootScope', '$routeParams', '$location', 'reportService',
	 function ($scope, $rootScope, $routeParams, $location, reportService) {
		
	$scope.quizID = $routeParams.quizId;
	
	$scope.$watch('quizID', function(newValue, oldValue) {
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
		reportService.loadAnswersReport($scope.quizID,
			function(response) {
				$scope.reportData = response.items;
			},
			function(error) {
		});
	};
	
}]);