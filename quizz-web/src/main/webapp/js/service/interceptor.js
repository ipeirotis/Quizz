angular.module('quizz').factory('interceptor', 
	['$rootScope', '$q', '$location', 'loading', function($rootScope, $q, $location, loading){
	return function(promise) {
		loading.show();
	
		return promise.then(
			function (response) {
				loading.hide();
				return response;
			}, 
			function (response) {
				if (response.status == 401) {
			        var deferred = $q.defer();
			        if($rootScope.url401 == ''){
			        	$rootScope.url401 = $location.url();
			        }
					$location.path("/login");
					loading.hide();
					return deferred.promise;
				}
				loading.hide();
				return $q.reject(response);
		});
	};
}]);