angular.module('quizz').factory('workflowService', [function() {
  var questions = {};
  var quizID = '';
  var userAnswers = [];
  var userFeedbacks = [];
  var currentQuestion = null;
  var currentQuestionIndex = 0;
  var currentGoldQuestionIndex = 0;
  var currentSilverQuestionIndex = 0;
  var numQuestions = 10;
  var numCalibrationQuestions = 0;
  var numCollectionQuestions = 0;
  var numCorrectAnswers = 0;
  var numSubmittedUserAnswers = 0;
  // TODO(chunhowt): Seems like we always choose the first question to be
  // explore even though the user might already have answered questions in
  // the same quizzes before.
  var isNextQuestionGold = true;
  var channelToken = '';
  var isCurrentQuestionGold = true;
  var bestAnswer = null;
  var userAnswerID = -1;

  return {
    clear: function(){
      questions = {};
      quizID = '';
      userAnswers = [];
      userFeedbacks = [];
      currentQuestion = null;
      currentQuestionIndex = 0;
      currentGoldQuestionIndex = 0;
      currentSilverQuestionIndex = 0;
      numCalibrationQuestions = 0;
      numCollectionQuestions = 0;
      numCorrectAnswers = 0;
      numSubmittedUserAnswers = 0;
      bestAnswer = null;
      userAnswerID = -1;
    },
    // Updates the workflow service with the new questions for the given quiz
    // id and reset the number of calibration and collection questions. The
    // newQuestionsMap is a map with length 2 and have the following
    // keys:
    // - calibration: Calibration questions.
    // - collection: Collection questions.
    setQuestions: function(newQuestionsMap, newQuizID) {
      quizID = newQuizID;
      questions = newQuestionsMap;
      if (newQuestionsMap.calibration) {
        numCalibrationQuestions = newQuestionsMap.calibration.length;
      }
      if (newQuestionsMap.collection) {
        numCollectionQuestions = newQuestionsMap.collection.length;
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
    // Picks the next question based on whether we are asking collection
    // or calibration questions (isNextQuestionGold).
    getNewCurrentQuestion: function() {
      // First, flip the bit of asking gold/silver question if one of them
      // is empty.
      if (numCalibrationQuestions == 0) {
        isNextQuestionGold = false;
      } else if (numCollectionQuestions == 0) {
        isNextQuestionGold = true;
      }
      // if we exhaust the number of calibration question, check whether
      // we can ask collection question instead.
      if (!isNextQuestionGold ||
          (currentGoldQuestionIndex >= numCalibrationQuestions &&
           numCollectionQuestions > 0)) {
        var index = currentSilverQuestionIndex % numCollectionQuestions;
        isCurrentQuestionGold = false;
        currentQuestion = questions.collection[index];
      } else {
        var index = currentGoldQuestionIndex % numCalibrationQuestions;
        isCurrentQuestionGold = true;
        currentQuestion = questions.calibration[index];
      }
      return currentQuestion;
    },
    getCurrentQuestion: function() {
      return currentQuestion;
    },
    getCurrentQuestionIndex: function() {
      return currentQuestionIndex;
    },
    getNumQuestions: function() {
      return numQuestions;
    },
    getNumCorrectAnswers: function() {
      return numCorrectAnswers;
    },
    getNumSubmittedUserAnswers : function() {
      return numSubmittedUserAnswers;
    },
    addUserAnswer: function(answer) {
      userAnswers.push(answer);
    },
    getLastAnswer: function() {
      return userAnswers[userAnswers.length - 1];
    },
    addUserFeedback: function(feedback) {
      userFeedbacks.push(feedback);
    },
    getLastFeedback: function() {
      return userFeedbacks[userFeedbacks.length - 1];
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
    incNumCorrectAnswers: function() {
      numCorrectAnswers++;
    },
    incNumSubmittedUserAnswers: function() {
      numSubmittedUserAnswers++;
    },
    setChannelToken: function(t) {
      channelToken = t;
    },
    getChannelToken: function() {
      return channelToken;
    },
    setNextQuestionGold: function(g) {
      isNextQuestionGold = g;
    },
    updateBestAnswer: function(newBestAnswer) {
      bestAnswer = newBestAnswer;
    },
    getBestAnswerId: function() {
      if (!bestAnswer) {
        return -1;
      }
      return bestAnswer.internalID;
    },
    getUserAnswerId: function() {
      return userAnswerID;
    },
    setUserAnswerId: function(newUserAnswerID) {
      userAnswerID = newUserAnswerID;
    }
  };
}]);
