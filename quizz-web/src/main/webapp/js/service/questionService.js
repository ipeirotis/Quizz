angular.module('quizz').factory('questionService', ['$http', function($http) {
  var options = {
      headers: {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      }
  };

  return {
    // Lists the next numQuestions of calibration and collection questions for
    // the given userid and quizid.
    // If successful, the success callback will be called with the response,
    // which is a map containing two values:
    //   - calibration - Set of calibration questions.
    //   - collection - Set of collection questions.
    list: function(numQuestions, quizId, userid, success, error) {
      var params = {
        num: numQuestions,
        quizID: quizId,
        userID: userid
      };
      var url = Config.api + '/listNextQuestions';
      $http.post(url, $.param(params), options).success(success).error(error);
    },
    // Sends the user answer to the appengine server for processing and getting
    // answer feedback.
    // If successful, the success callback will be called with the response,
    // which is a map containing four values:
    //   - userAnswer: UserAnswer entity.
    //   - userAnswerFeedback: UserAnswerFeedback entity.
    //   - exploit: Whether the next question should be an exploit.
    //   - bestAnswer: Best answer for the given question.
    sendAnswer: function(quizID, questionID, answerID, userID, userInput,
        currCorrectAnswers, currIncorrectAnswers, success, error) {
      var params = {
        quizID: quizID,
        questionID: questionID,
        answerID: answerID,
        userID: userID,
        userInput: userInput,
        numCorrect: currCorrectAnswers,
        numIncorrect: currIncorrectAnswers,
        numExploit: 0
      };
      $http.post(Config.api + '/processUserAnswer', $.param(params), options)
           .success(success).error(error);
    },
    // Gets the quiz performance of the given username in the quizId.
    // If successful, the success callback will be called with the response,
    // which is a QuizPerformance entity.
    markConversion: function(quizId, username, success, error) {
      var url = Config.api + '/getQuizPerformance';
      var params = {
        quizID : quizId,
        userID : username
      };
      $http.post(url, $.param(params), options).success(success).error(error);
    },
    // Adds the given message as the answer challenge text for the given
    // user answer.
    // If successful, the success callback will be called with the response,
    // which is a UserAnswer entity.
    challengeAnswer: function(quizID, questionID, userAnswerID, userID, message,
        correctValue, urlSupport, challengeReason, success, error) {
      var params = {
        quizID: quizID,
        questionID: questionID,
        userAnswerID: userAnswerID,
        userid: userID,
        message: message,
        correctValue: correctValue,
        urlSupport: urlSupport,
        challengeReason: challengeReason
      };
      $http.post(Config.api + '/addAnswerFeedback', $.param(params), options)
           .success(success).error(error);
    }
  };
}]);
