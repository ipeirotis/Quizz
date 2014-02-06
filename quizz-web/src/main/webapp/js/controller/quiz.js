angular.module('quizz').controller('QuizController', 
	['$scope', '$rootScope', '$routeParams', '$location', 'questionService', 'quizService', 'workflowService', 'userService',
	 function ($scope, $rootScope, $routeParams, $location, questionService, quizService, workflowService, userService) {
		
		$scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
		$scope.numOfQuestions = workflowService.getNumOfQuestions();
		
		$rootScope.$on("event:channel", function (event, data) {
			console.log(data);
			$scope.performance = data;
		});
		
		$scope.fetchQuestions = function(){
			questionService.list($scope.numOfQuestions, $routeParams.quizId,
				function(response) {
					workflowService.setQuestions(response);
					
					$scope.currentQuestion = workflowService.getCurrentQuestion();
					$scope.readyToShow = true;
				},
				function(error) {
			});
		};
		
		$scope.fetchQuestions();
		
		$scope.fetchUserQuizPerformance = function(){
			quizService.getUserQuizPerformance($routeParams.quizId, userService.getUsername(),
				function(response) {
				$scope.performance = response;
				$scope.showPerformance = true;
			});
		};
		
		$scope.fetchUserQuizPerformance();
		
		$scope.answerQuestion = function(answerID, gaType, userInput){			
			var params = {
				quizID: $routeParams.quizId,
				questionID: workflowService.getCurrentQuestion().id,
				answerID: answerID,
				userInput: userInput || '',
				totalanswers: 0,//TODO
				correctanswers: 0,
				a: 0,
				b: 0,
				c: 0
			};
			questionService.sendAnswer(params,
				function(response) {
					workflowService.addUserAnswer(response.userAnswer);
					workflowService.addUserFeedback(response.userAnswerFeedback);
					$scope.showFeedback();
				},
				function(error) {
			});
			
			questionService.markConversion(gaType, $routeParams.quizId,
					userService.getUsername());
		};
		
		$scope.showFeedback = function(){
			$location.path('/feedback');
		};
		
		$scope.getGaType = function(answerKind){
			if(answerKind == 'selectable_gold'){
				return 'multiple-choice-correct';
			}else if(answerKind == 'selectable_not_gold'){
				return 'multiple-choice-incorrect';
			}else if(answerKind == 'input_text'){
				return 'input-text-correct';
			}
		};
		
		$scope.filterSelectable = function(answer) {
	        return answer.kind == 'selectable_gold'
	        	|| answer.kind == 'selectable_not_gold';
	    };
	    
		$scope.filterNotSelectable = function(answer) {
	        return answer.kind == 'input-text-correct';
	    };

	    $scope.ranksFormating = function(userValue, totalValue) {
			var prefix = "Rank: ";
			var sufix = "---";
			if ($scope.isNormalNumber(userValue) && $scope.isNormalNumber(totalValue)) {
				var position = userValue / totalValue;
				sufix = "" + userValue + "/" + totalValue + " (Top " + $scope.toPercentage(position) + ")";
			}
			return prefix + sufix;
		};
		
		$scope.isNormalNumber = function(value) {
			return ! (isNaN(value) || typeof value === undefined);
		};
		
		$scope.toPercentage = function(fValue) {
			return (100. * fValue).toFixed(0) + "%";
		};
		
}]);