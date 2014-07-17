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
          $.cookie("username", response.userid,
                   { expires: 60, path: "/", secure: true });
        }
      }
      // Calls fetchQuizes after creating user cookies as we need them to show
      // user progress.
      $scope.fetchQuizes();
    },
    function(error) {}
  );
}]);
