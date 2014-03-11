angular.module('quizz').factory('userService', ['$http', '$rootScope', function($http, $rootScope){
  var options = {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
    return {
      getUser: function(success, error) {
        var url = Config.api + '/user?userid=' + this.getUsername();
          $http.get(url).success(success).error(error);
      },
      getUsername: function() {
        var username = $.cookie("username");
        if (!username) {
          username = this.createUsername();
        }
        return username;
      },
      createUsername: function() {
        var username = $rootScope.utils.createUUID();
        $.cookie("username", username, { expires: 365, path: "/"});
        return username;
      },
    };
}]);
