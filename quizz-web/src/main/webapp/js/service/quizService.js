angular.module('quizz').factory('quizService',
  ['$http', '$q', '$rootScope', '$cacheFactory',
   function($http, $q, $rootScope, $cacheFactory) {
  var options = {headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
  var keys = [];
  var cache = $cacheFactory('quizCache');

  return {
    // Lists the quizzes.
    // If successful, the success callback will be called with the response,
    // which is a list of Quiz entities.
    list: function(userID, success, error) {
      var self = this;
      var params = {
        userid : userID
      };
      var userQuizPerformancesRequest =
          $http.post(Config.api + '/listQuizPerformanceByUser', $.param(params),
                     options);

      var quizRequest;
      if (keys.length == 0) {
        quizRequest = $http.get(Config.api + '/listQuiz');
      } else {
        quizRequest = $q.when(this.getQuizesFromCache());
      }

      $q.all([quizRequest, userQuizPerformancesRequest])
        .then(function(arrayOfResults) {
          var quizes = arrayOfResults[0].data.items;
          var userQuizPerformances = arrayOfResults[1].data.items;

          angular.forEach(userQuizPerformances, function(userQuizPerformance) {
            angular.forEach(quizes, function(quiz) {
              if (quiz.quizID == userQuizPerformance.quiz) {
                quiz['numUserAnswers'] = userQuizPerformance.totalanswers;
              }
            });
          });

          self.cacheQuizes(quizes);
          if (angular.isFunction(success)) {
            success(quizes);
          }
        });
    },
    // Returns the quiz performance of the given userID in the quizID.
    // If successful, the success callback will be called with the response,
    // which is a QuizPerformance entity.
    getUserQuizPerformance: function(quizID, userID, success, error) {
      var url = Config.api + '/getQuizPerformance';
      var params = {
        quizID : quizID,
        userID : userID
      };
      $http.post(url, $.param(params), options).success(success).error(error);
    },
    // Caches the given quizzes entities in the cache factory.
    cacheQuizes: function(quizes) {
      keys = [];
      angular.forEach(quizes, function(value, key) {
        cache.put(value.quizID, value);
        keys.push(value.quizID);
      });
    },
    // Gets the list of quizzes entities from the cache factory in the form
    // of the CollectionResponse<Quiz> form.
    getQuizesFromCache: function() {
      var result = [];
      angular.forEach(keys, function(value, key) {
        var quiz = cache.get(value);
        delete quiz['numUserAnswers'];
        result.push(quiz);
      });
      return {data:{items: result}};
    }
  };
}]);
