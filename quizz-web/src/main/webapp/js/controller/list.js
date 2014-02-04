angular.module('quizz').controller('ListController', 
	['$scope', 'quizService', 'userService', function ($scope, quizService, userService) {
		
	$scope.fetchQuizes = function(){
		quizService.list(userService.getUsername(),
			function(response) {
				$scope.quizes = response;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.fetchQuizes();
	
}]);