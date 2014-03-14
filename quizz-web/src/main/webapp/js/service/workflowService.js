angular.module('quizz').factory('workflowService', [function() {
  var questions = {};
  var userAnswers = [];
  var userFeedbacks = [];
  var currentQuestion = null;
  var currentQuestionIndex = 0;
  var currentGoldQuestionIndex = 0;
  var currentSilverQuestionIndex = 0;
  var numOfQuestions = 10;
  var numOfGoldQuestions = 0;
  var numOfSilverQuestions = 0;
  var numOfCorrectAnswers = 0;
  var isNextQuestionGold = true;
  var channelToken = '';
  var isCurrentQuestionGold = true;

  return {
    clear: function(){
      questions = {};
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
    hasQuestions: function() {
      return questions.gold || questions.silver;
    },
    getIsCurrentQuestionGold: function() {
      return isCurrentQuestionGold;
    },
    getNewCurrentQuestion: function() {
      // For now, randomly pick whether it is a gold or a silver question until
      // the exploration-exploitation is fixed.
      // TODO(chunhowt): This assumes at least one of gold/silver is non-empty.
      var rnd = Math.random();
      if (numOfGoldQuestions == 0) {
        useGold = false;
      } else if (numOfSilverQuestions == 0) {
        useGold = true;
      } else {
        useGold = Math.random() < 0.5;
      }
      if (!useGold) {
        currentQuestion =
            questions.silver[currentQuestionIndex % numOfSilverQuestions];
        isCurrentQuestionGold = false;
      } else {
        currentQuestion =
            questions.gold[currentQuestionIndex % numOfGoldQuestions];
        isCurrentQuestionGold = true;
      }
      return currentQuestion;
    },
    getCurrentQuestion: function() {
      return currentQuestion;
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
