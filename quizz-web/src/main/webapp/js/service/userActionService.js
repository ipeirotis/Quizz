angular.module('quizz').factory('userActionService', ['$http', function($http) {
  var options = {headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
  return {
    recordUserQuizAction: function(
        userID, quizID, questionID, url, success, error) {
      var params = {
        userID: userID,
        quizID: quizID,
        questionID: questionID
      };
      $http.post(url, $.param(params), options).success(success).error(error);
    },
    recordQuestionShown: function(userID, quizID, questionID, success, error) {
      var url = Config.api + '/recordQuestionShown';
      this.recordUserQuizAction(
          userID, quizID, questionID, url, success, error);
    },
    recordExpandQuestionContext: function(
        userID, quizID, questionID, success, error) {
      var url = Config.api + '/recordExpandQuestionContext';
      this.recordUserQuizAction(
          userID, quizID, questionID, url, success, error);
    },
    recordHideQuestionContext: function(
        userID, quizID, questionID, success, error) {
      var url = Config.api + '/recordHideQuestionContext';
      this.recordUserQuizAction(
          userID, quizID, questionID, url, success, error);
    }
  };
}]);
