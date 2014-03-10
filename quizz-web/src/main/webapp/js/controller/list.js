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

  $scope.fetchQuizes();

  $scope.fetchUser = function() {
    userService.getUser(
      function(response) {
        workflowService.setChannelToken(response.token);
        $scope.setupChannel(response.token);
      },
      function(error) {
      });
  };

  $scope.fetchUser();

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
}]);
