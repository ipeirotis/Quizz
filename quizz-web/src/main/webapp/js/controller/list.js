angular.module('quizz').controller('ListController',
    ['$scope', '$rootScope', 'quizService', 'userService', 'workflowService',
     function ($scope, $rootScope, quizService, userService, workflowService) {
  $scope.fetchQuizes = function() {
    quizService.list(
      userService.getUsername(),
      function(response) {
        $scope.quizes = response;
        $scope.readyToShow = true;
      },
      function(error) {
      });
  };

  $scope.setupChannel = function(token) {
    var channel = new goog.appengine.Channel(token);
    var socket = channel.open();
    socket.onmessage = function(data) {
      $rootScope.$broadcast('event:channel', data);
    };
    socket.onerror = function () {
      console.log("Error in channel gathering updates in performance");
    };
  };

  // Filter for the given quiz to return true only if quiz.showOnDefault is
  // defined and true.
  $scope.filterVisible = function(quiz) {
    return quiz.showOnDefault;
  };

  // Gets or creates a new user id.
  userService.maybeCreateUser(
    null,  // null quiz id.
    function(response) {
      if (response) {
        if (response.userid) {
          $.cookie("username", response.userid, { expires: 365, path: "/" });
        }
        if (response.token) {
          workflowService.setChannelToken(response.token);
          $scope.setupChannel(response.token);
        }
      }
      // Calls fetchQuizes after creating user cookies as we need them to show
      // user progress.
      $scope.fetchQuizes();
    },
    function(error) {}
  );
}]);
