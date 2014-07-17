angular.module('quizz').factory('userService', ['$http', function($http) {
  var options = {headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
  // A flag to indicate whether we have called maybeCreateUser at least once.
  // This is allow us to verify the validity of the cookie and log a user
  // referal each time a user access Quizz from somewhere else.
  // Note: This variable is alive as long as the angular ngApp, and thus will
  // be destroyed and recreated if the user reloads the page.
  var isInit = false;
    return {
      maybeCreateUser: function(quizID, success, error) {
        if ($.cookie('username') && isInit) {
          success(null);
          return;
        } else {
          isInit = true;
        }
        var params = {
          referer: document.referrer,
        };
        if (quizID) {
          params.quizID = quizID;
        }
        if ($.cookie('username')) {
          params.userID = $.cookie('username');
        }
        var url = Config.api + '/getUser';
        $http.post(url, $.param(params), options).success(success).error(error);
      },
      getUsername: function() {
        return $.cookie("username");
      },
    };
}]);
