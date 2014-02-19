angular.module('quizz-admin').factory('reportService', ['$http', function($http){
    return {
    	load: function(quizId, success, error) {
    		var url = Config.api + '/report?quizID=' + quizId;
        	$http.get(url).success(success).error(error);
        },
    	listQuizes: function(success, error) {
        	$http.get(Config.api + '/quiz').success(success).error(error);
        }
    };
}]);