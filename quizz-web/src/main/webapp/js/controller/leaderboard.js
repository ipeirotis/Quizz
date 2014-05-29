angular.module('quizz').controller('LeaderboardController',
  ['$scope', '$rootScope', '$routeParams', 'quizService', 
   function ($scope, $rootScope, $routeParams, quizService) {

     $scope.fetchTopQuizPerformance = function() {
       quizService.getTopQuizPerformance($routeParams.quizId, 10,
         function(response) {
           $scope.topPerformance = response.items;
         });
     };

     $scope.fetchTopQuizPerformance();
     // $scope.topTenOption = {
     //  data: 'topPerformance'
     // }
  }]
);
