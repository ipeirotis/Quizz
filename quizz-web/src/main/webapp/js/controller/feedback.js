angular.module('quizz').controller('FeedbackController', 
		['$scope', '$routeParams', '$location', '$q', '$modal', 'questionService', 'workflowService',
		 function ($scope, $routeParams, $location, $q, $modal, questionService, workflowService) {

	$scope.feedback = workflowService.getLastFeedback();
	$scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
	$scope.numOfQuestions = workflowService.getNumOfQuestions();	
	
	$scope.challengeAnswer = function() {
		var modalPromise = $modal({template: '/views/modals/challengeAnswer.html', 
			persist: false, show: false, keyboard: true, 
			data: {}});
		
		$q.when(modalPromise).then(function(modalEl) {
		    modalEl.modal('show');
		});
	};
	
	$scope.nextQuestion = function(){
		workflowService.incCurrentQuestionIndex();
		
		if(workflowService.getCurrentQuestionIndex() < workflowService.getNumOfQuestions()){
			$location.path('/quiz');
		} else {
			$location.path('/summary');
		}
	};
	
}]);