angular.module('quizz').factory('quizService',
  ['$http', '$q', '$rootScope', '$cacheFactory',
   function($http, $q, $rootScope, $cacheFactory) {
  var options = {headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
  var keys = [];
  var cache = $cacheFactory('quizCache');

  return {
    list: function(user, success, error) {
      var self = this;
      var params = {
        userid : user
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
                quiz['totalanswers'] = userQuizPerformance.totalanswers;
              }
            });
          });

          self.cacheQuizes(quizes);

          if (angular.isFunction(success)) {
            success(quizes);
          }
        });
    },
    getUserQuizPerformance: function(quiz, user, success, error) {
      var url = Config.api + '/getQuizPerformance';
      var params = {
        quizID : quiz,
        userID : user
      };
      $http.post(url, $.param(params), options).success(success).error(error);
    },
    cacheQuizes: function(quizes) {
      keys = [];
      angular.forEach(quizes, function(value, key) {
        cache.put(value.quizID, value);
        keys.push(value.quizID);
      });
    },
    getQuizesFromCache: function() {
      var result = [];
      angular.forEach(keys, function(value, key) {
        result.push(cache.get(value));
      });
      return {data:{items: result}};
    }
  };
}]);
