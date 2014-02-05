angular.module('quizz').controller('SummaryController', 
	['$scope', '$routeParams', '$location', 'workflowService', 
	 function ($scope, $routeParams, $location, workflowService) {
	
	$scope.numOfQuestions = workflowService.getNumOfQuestions();
	
	$scope.getCorrectAnswersCount = function(){
		var count = 0;
       	angular.forEach(workflowService.getUserFeedbacks(), function(feedback){
       		if(feedback.isCorrect)
       			count++;
		});
       	
       	return count;
	};
	
	$scope.shareFacebook = function(){
		 window.open('http://www.facebook.com/sharer/sharer.php?s=100' +
         		'&p[url]=' + encodeURIComponent('http://www.quizz.us') +
         		'&p[images][0]=' +
         		'&p[title]=I+have+just+answered+' + $scope.getCorrectAnswersCount() + 
         		'+out+of+' + $scope.numOfQuestions + '+questions+correctly!',
           'facebook-share-dialog',
           'width=626,height=436');
         return false;
	};
	
	$scope.startAgain = function(){
		workflowService.clear();
		$location.path('/quiz');
	};
	
	gapi.plus.go();//TODO: check
}]);