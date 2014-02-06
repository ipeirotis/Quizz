angular.module('quizz').factory('workflowService', [function(){
	
	var questions = {};
	var userAnswers = [];
	var userFeedbacks = [];
	var currentQuestionIndex = 0;
	var currentGoldQuestionIndex = 0;
	var currentSilverQuestionIndex = 0;
	var numOfQuestions = 10;
	var numOfGoldQuestions = 0;
	var numOfSilverQuestions = 0;
	var numOfCorrectAnswers = 0;
	var isNextQuestionGold = true;
	var channelToken = '';
	
	return {
		clear: function(){
			userAnswers = [];
			userFeedbacks = [];
			currentQuestionIndex = 0;
			numOfCorrectAnswers = 0;
			numOfGoldQuestions = 0;
			numOfSilverQuestions = 0;
		},
		setQuestions: function(q) {
			questions = q;
			if(q.gold){
				numOfGoldQuestions = q.gold.length;
			}
			if(q.silver){
				numOfSilverQuestions = q.silver.length;
			}
		},
		getQuestions: function() {
			return questions;
		},
		getCurrentQuestion: function() {
			if((isNextQuestionGold == true && currentQuestionIndex < numOfGoldQuestions) ||
					currentQuestionIndex >= numOfSilverQuestions){
				return questions.gold[currentQuestionIndex];
			}else{
				return questions.silver[currentQuestionIndex];
			}
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
        	currentGoldQuestionIndex++;
        	currentSilverQuestionIndex++;
        },
        setChannelToken: function(t) {
        	channelToken = t;
		},
		getChannelToken: function(){
			return channelToken;
		},
		setNextQuestionGold: function(g){
			isNextQuestionGold = g;
		}
    };
	
}]);