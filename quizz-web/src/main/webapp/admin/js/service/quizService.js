angular.module('quizz-admin').factory('quizService',
    ['$http', '$cacheFactory', function($http, $cacheFactory) {

  function callbackWrapper(resp, success, error) {
    if (!resp.code && angular.isFunction(success)) {
      success(resp);
    } else if (angular.isFunction(error)) {
      error(resp);
    }
  };

  return {
    getQuiz: function(quizID, success, error) {
      $http.get(Config.api + '/getQuiz', {'params': {quizID: quizID}}).success(success).error(error);
    }
  };
}]);
