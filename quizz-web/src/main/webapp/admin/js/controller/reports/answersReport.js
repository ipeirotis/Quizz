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
				$scope.answersKind = $scope.getAnswersKind(response.items);
				$scope.reportData = response.items;
			},
			function(error) {
		});
	};
	
	$scope.getAnswersKind = function(items){
		var result = '';
       	angular.forEach(items, function(item){	
			if(item.answers && item.answers.length > 0){
				var kind = item.answers[0].kind;
				if(kind == 'selectable_not_gold' || kind == 'selectable_gold'){
					result = 'SELECTABLE';
				}else{
					result = 'INPUT';
				}
				return;
			}
		});
       	return result;
	};
	
}]);