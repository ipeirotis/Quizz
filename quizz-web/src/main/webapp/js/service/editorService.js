angular.module('quizz').factory('editorService', ['$http', function($http) {

  return {
    listQuizzes: function(success, error) {
      $http.get(Config.api + '/listQuiz').success(success).error(error);
    },

    getQuiz: function(quizID, success, error) {
      $http.get(Config.api + '/getQuiz', {'params': {quizID: quizID}}).success(success).error(error);
    },
    
    saveQuiz: function(quiz, success, error) {
      $http.post(Config.api + '/insertQuiz', quiz).success(success).error(error);
    },

    removeQuiz: function(quizId, success, error) {

    },
    
    listQuestions: function(quizId, success, error) {
      $http.get(Config.api + '/listAllQuestions', {'params': {quizID: quizId}}).success(success).error(error);
    },

    getQuestion: function(id, success, error) {
      $http.get(Config.api + '/getQuestion', {'params': {id: id}}).success(success).error(error);
    },

    saveQuestion: function(question, success, error) {
      $http.post(Config.api + '/insertQuestion', question).success(success).error(error);
    },

    removeQuestion: function(questionId, success, error) {

    }
  };
}]);
