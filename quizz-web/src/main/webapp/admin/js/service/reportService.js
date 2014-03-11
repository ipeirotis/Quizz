angular.module('quizz-admin').factory('reportService', 
		['$http', '$cacheFactory', function($http, $cacheFactory){
	var LIMIT = 20;
	var pageTokens = [];
	var cache = $cacheFactory('domainsReportCache');
	
    return {
    	loadAnswersReport: function(quizId, success, error) {
    		var url = Config.api + '/reports/multiChoiceAnswers?quizID=' + quizId;
        	$http.get(url).success(success).error(error);
        },
    	loadScoreByBrowserReport: function(success, error) {
    		var url = Config.api + '/reports/scoreByBrowser';
        	$http.get(url).success(success).error(error);
        },
    	loadScoreByDomainReport: function(pageNumber, success, error) {
    		if(pageNumber > 0 && pageNumber < pageTokens.length){
    			var items = cache.get(pageTokens[pageNumber]);
	        	if(items && angular.isFunction(success)){
	        		success(items);
	        		return;
	        	}
    		}
    		var url = Config.api + '/reports/scoreByDomain?limit=' + LIMIT;
    		if(pageNumber > 0){
    			console.log(pageTokens.length);
    			url += '&cursor=' + pageTokens[pageNumber-1];
    		}
        	$http.get(url).success(function(response) {
				if(response.nextPageToken){
					pageTokens.push(response.nextPageToken);
					cache.put(pageTokens[pageNumber], response.items);
				}
	        	if(angular.isFunction(success)){
	        		success(response.items);
	        	}
	        }).error(error);
        },
    	loadContributionQualityReport: function(success, error) {
    		var url = Config.api + '/reports/contributionQuality';
        	$http.get(url).success(success).error(error);
        },
    	listQuizes: function(success, error) {
        	$http.get(Config.api + '/listQuiz').success(success).error(error);
        }
    };
}]);
