angular.module('quizz').factory('quizService', 
	['$http', '$q', '$rootScope', '$cacheFactory', function($http, $q, $rootScope, $cacheFactory){

	var keys = [];
	var cache = $cacheFactory('quizCache');
	
    return {
    	list: function(user, success, error) {
    		var self = this;
    		var userQuizPerformancesRequest = 
    			$http.get(Config.api + '/quizperformance/user/' + encodeURIComponent(user));

    		var quizRequest;
    			
    		if(keys.length == 0) {
    			quizRequest = $http.get(Config.api + '/quiz');
    		} else {
    			quizRequest = $q.when(this.getQuizesFromCache());
    		}
    		
    		$q.all([quizRequest, userQuizPerformancesRequest]).then(function(arrayOfResults) {
    			var quizes = arrayOfResults[0].data.items;
    			var userQuizPerformances = arrayOfResults[1].data.items;
    			
    	       	angular.forEach(userQuizPerformances, function(userQuizPerformance){
        	       	angular.forEach(quizes, function(quiz){
        	       		if(quiz.quizID == userQuizPerformance.quiz){
        	       			quiz['totalanswers'] = userQuizPerformance.totalanswers;
        	       		}
        			});
    			});
    			
    			self.cacheQuizes(quizes);
    			
        		if(angular.isFunction(success)){
        			success(quizes);
        		}
    		});
        },
        getUserQuizPerformance: function(quiz, user, success, error){
        	var url = Config.api + '/quizperformance';
    		url += '/quiz/' + encodeURIComponent(quiz);
    		url += '/user/' + encodeURIComponent(user);
    		
    		$http.get(url).success(success).error(error);
        },
        cacheQuizes: function(quizes){
        	keys = [];
	       	angular.forEach(quizes, function(value, key){
				cache.put(value.quizID, value);
				keys.push(value.quizID);
			});
        },
        getQuizesFromCache: function(){
			var result = [];
			angular.forEach(keys, function(value, key){
				result.push(cache.get(value));
			});
			return {data:{items: result}};
        }
    };
}]);