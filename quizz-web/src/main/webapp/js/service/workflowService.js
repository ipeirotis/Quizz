angular.module('quizz').factory('workflowService', [function() {
  var questions = {};
  var userAnswers = [];
  var userFeedbacks = [];
  var currentQuestion = null;
  var currentQuestionIndex = 0;
  var currentGoldQuestionIndex = 0;
  var currentSilverQuestionIndex = 0;
  var numOfQuestions = 10;
  var numOfCalibrationQuestions = 0;
  var numOfCollectionQuestions = 0;
  var numOfCorrectAnswers = 0;
  var isNextQuestionGold = true;
  var channelToken = '';
  var isCurrentQuestionGold = true;
  var quizID = '';

  return {
    clear: function(){
      questions = {};
      quizID = '';
      userAnswers = [];
      userFeedbacks = [];
      currentQuestionIndex = 0;
      currentGoldQuestionIndex = 0;
      currentSilverQuestionIndex = 0;
      numOfCorrectAnswers = 0;
      numOfCalibrationQuestions = 0;
      numOfCollectionQuestions = 0;
    },
    setQuestions: function(q, newQuizID) {
      quizID = newQuizID;
      questions = q;
      if (q.calibration) {
        numOfCalibrationQuestions = q.calibration.length;
      }
      if (q.collection) {
        numOfCollectionQuestions = q.collection.length;
      }
    },
    getCurrentQuizID: function() {
      return quizID;
    },
    getQuestions: function() {
      return questions;
    },
    hasQuestions: function() {
      return questions.calibration || questions.collection;
    },
    getIsCurrentQuestionGold: function() {
      return isCurrentQuestionGold;
    },
    getNewCurrentQuestion: function() {
      if (numOfCalibrationQuestions == 0) {
        isNextQuestionGold = false;
      } else if (numOfCollectionQuestions == 0) {
        isNextQuestionGold = true;
      }
      if (isNextQuestionGold) {
        var index = currentGoldQuestionIndex % numOfCalibrationQuestions;
        currentQuestion = questions.calibration[index];
        isCurrentQuestionGold = true;
      } else {
        var index = currentSilverQuestionIndex % numOfCollectionQuestions;
        currentQuestion = questions.collection[index];
        isCurrentQuestionGold = false;
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
      if (isCurrentQuestionGold) {
        currentGoldQuestionIndex++;
      } else {
        currentSilverQuestionIndex++;
      }
    },
    incNumOfCorrectAnswers: function() {
      numOfCorrectAnswers++;
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
