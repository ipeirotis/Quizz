angular.module('quizz-admin').controller('MenuController', 
	['$scope', '$location',	function ($scope, $location) {
	
	$scope.isActive = function (view) {
		if($location.path().indexOf(view) == 0 ){
			return true;
		} else {
			return false;
		}
    };
    
}]);