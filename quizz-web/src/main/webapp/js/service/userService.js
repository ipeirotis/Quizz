angular.module('quizz').factory('userService', ['$http', function($http) {
  var options = {headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
    return {
      maybeCreateUser: function(success, error) {
        if ($.cookie("username")) {
          success(null);
          return;
        }
        var params = {
          referer: document.referrer
        };
        var url = Config.api + '/getUser';
        $http.post(url, $.param(params), options).success(success).error(error);
      },
      getUsername: function() {
        return $.cookie("username");
      },
    };
}]);
