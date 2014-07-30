angular.module('quizz').factory('editorService', ['$http', '$cacheFactory', function($http, $cacheFactory) {

  var keys = [];
  var cache = $cacheFactory('editorQuizCache');

  return {
    listQuizzes: function(success, error) {

      if(keys.length == 0){
        $http.get(Config.api + '/listQuiz').success(function(response) {
          angular.forEach(response.items, function(quiz){
            cache.put(quiz.quizID, quiz);
            keys.push(quiz.quizID);
          });

          if(angular.isFunction(success)){
            success(response.items);
          }
        }).error(error);
      }else{
        if(angular.isFunction(success)){
          var result = [];
          angular.forEach(keys, function(key){
            result.push(cache.get(key));
          });
          success(result);
        }
      }
    },

    getQuiz: function(quizID, success, error) {
      var fromCache = cache.get(quizID);
      if(!fromCache){
        $http.get(Config.api + '/getQuiz', {'params': {quizID: quizID}}).success(function(response) {
          cache.put(quizID, response);
          keys.push(quizID);

          if(angular.isFunction(success)){
            success(response);
          }
        }).error(error);
      }else{
        if(angular.isFunction(success)){
          success(fromCache);
        }
      }
    },
    
    saveQuiz: function(quiz, success, error) {
      $http.post(Config.api + '/insertQuiz', quiz).success(function(savedQuiz) {console.log(savedQuiz)
        cache.put(savedQuiz.quizID, savedQuiz);
        var i = $.inArray(savedQuiz.quizID, keys);
        if(i < 0) {
          keys.push(savedQuiz.quizID);
        }

        if(angular.isFunction(success)){
          success(savedQuiz);
        }
      }).error(error);
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

    removeQuestion: function(id, success, error) {
      $http['delete'](Config.api + '/removeQuestion/' + id).success(success).error(error);
    }
  };
}]);
