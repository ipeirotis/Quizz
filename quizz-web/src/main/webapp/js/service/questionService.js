angular.module('quizz').factory('questionService', ['$http', function($http){
	var options = {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
	
    return {
    	list: function(numOfQuestions, quizId, success, error) {
    		var url = Config.api + '/quizquestions/' + quizId + '?num=' + numOfQuestions;
        	$http.get(url).success(success).error(error);
        },
        sendAnswer: function(params, success, error){
        	$http.post(Config.api + '/processUserAnswer', $.param(params), options)
        		.success(success).error(error);
        },
        markConversion: function(type, quizId, username){
			var url = Config.api + '/quizperformance';
			url += '/quiz/' + quizId;
			url += '/user/' + username;
			
        	$http.get(url).success(function(response){
        		ga('send', {
	    			  'hitType': 'event',
	    			  'hitCallback': function(){ },
	    			  'eventCategory': 'quiz-submission',
	    			  'eventAction': type,
	    			  'eventLabel': quizId,
	    			  'eventValue': Math.round(100.*response.score/response.totalanswers),
	    			  } );
        	});
        },
        challengeAnswer: function(params, success, error){
        	$http.post(Config.api + '/addAnswerFeedback', $.param(params), options)
        		.success(success).error(error);
        }
    };
}]);
