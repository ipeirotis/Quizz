angular.module('quizz').factory('workflowService', [function(){
	
	var questions = {};
	var userAnswers = [];
	var userFeedbacks = [];
	var currentQuestionIndex = 0;
	var numOfQuestions = 10;
	var numOfCorrectAnswers = 0;
	var channelToken = '';
	
	return {
		clear: function(){
			userAnswers = [];
			userFeedbacks = [];
			currentQuestionIndex = 0;
			numOfCorrectAnswers = 0;
		},
		setQuestions: function(q) {
			questions = q;
		},
		getQuestions: function() {
			return questions;
		},
		getCurrentQuestion: function() {
			return questions.gold[currentQuestionIndex];
		},
		getCurrentQuestionIndex: function() {
        	return currentQuestionIndex;
        },
		getNumOfQuestions: function() {
			return numOfQuestions;
		},
		getNumOfCorrectAnswers: function() {
			return numOfCorrectAnswers;
		},
        addUserAnswer: function(answer) {
        	userAnswers.push(answer);
        },
        getLastAnswer: function() {
        	return userAnswers[userAnswers.length-1];
        },
        addUserFeedback: function(feedback) {
        	userFeedbacks.push(feedback);
        },
        getLastFeedback: function() {
        	return userFeedbacks[userFeedbacks.length-1];
        },
        getUserFeedbacks: function() {
        	return userFeedbacks;
        },
        incCurrentQuestionIndex: function() {
        	currentQuestionIndex++;
        },
        setChannelToken: function(t) {
        	channelToken = t;
		},
		getChannelToken: function(){
			return channelToken;
		}
    };
	
}]);