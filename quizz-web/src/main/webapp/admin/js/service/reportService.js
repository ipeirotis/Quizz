angular.module('quizz-admin').factory('reportService', ['$http', function($http){
    return {
    	loadAnswersReport: function(quizId, success, error) {
    		var url = Config.api + '/reports/answers?quizID=' + quizId;
        	$http.get(url).success(success).error(error);
        },
    	loadScoreByBrowserReport: function(success, error) {
    		var url = Config.api + '/reports/scoreByBrowser';
        	$http.get(url).success(success).error(error);
        },
    	loadScoreByDomainReport: function(success, error) {
    		var url = Config.api + '/reports/scoreByDomain';
        	$http.get(url).success(success).error(error);
        },
    	loadContributionQualityReport: function(success, error) {
    		var url = Config.api + '/reports/contributionQuality';
        	$http.get(url).success(success).error(error);
        },
    	listQuizes: function(success, error) {
        	$http.get(Config.api + '/quiz').success(success).error(error);
        }
    };
}]);