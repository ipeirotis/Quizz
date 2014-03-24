angular.module('quizz').controller('QuizController',
  ['$scope', '$rootScope', '$routeParams', '$location', 'questionService',
   'quizService', 'workflowService', 'userService',
   function ($scope, $rootScope, $routeParams, $location, questionService,
             quizService, workflowService, userService) {
     $scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
     $scope.numOfQuestions = workflowService.getNumOfQuestions();
     $rootScope.$on("event:channel", function (event, data) {
       $scope.performance = data;
     });

     // Gets or creates a new user id if user directly arrives at the question
     // page.
     userService.getUser(
       function(response) {},
       function(error) {});

     $scope.fetchQuestions = function() {
       // If we don't have questions set in the workflowService, fetch them.
       if (!workflowService.hasQuestions()) {
         questionService.list($scope.numOfQuestions, $routeParams.quizId,
           function(response) {
             workflowService.setQuestions(response);

             $scope.currentQuestion = workflowService.getNewCurrentQuestion();
             $scope.readyToShow = true;
           },
           function(error) {
         });
       } else {
         // Else, reuse the existing questions.
         $scope.currentQuestion = workflowService.getNewCurrentQuestion();
         $scope.readyToShow = true;
       }
     };

     $scope.fetchQuestions();

     $scope.fetchUserQuizPerformance = function() {
       quizService.getUserQuizPerformance($routeParams.quizId, userService.getUsername(),
         function(response) {
           $scope.performance = response;
           $scope.showPerformance = true;
         });
     };

     $scope.fetchUserQuizPerformance();

     $scope.answerQuestion = function(answerID, gaType, userInput) {
       var params = {
         quizID: $routeParams.quizId,
         questionID: workflowService.getCurrentQuestion().id,
         answerID: answerID,
         userID: userService.getUsername(),
         userInput: userInput || '',
         totalanswers: $scope.performance.totalanswers,
         correctanswers: $scope.performance.correctanswers,
         a: workflowService.getNumOfCorrectAnswers(),
         b: workflowService.getNumOfQuestions()-workflowService.getNumOfCorrectAnswers(),
         c: 0
       };
       questionService.sendAnswer(params,
         function(response) {
           workflowService.addUserAnswer(response.userAnswer);
           workflowService.addUserFeedback(response.userAnswerFeedback);
           workflowService.setNextQuestionGold(response.exploit);
           $scope.showFeedback();
         },
         function(error) {
         });

       questionService.markConversion(gaType, $routeParams.quizId,
           userService.getUsername());
     };

     $scope.showFeedback = function() {
       $location.path('/feedback');
     };

     $scope.getGaType = function(answerKind) {
       if (answerKind == 'GOLD' || answerKind == 'SILVER') {
         return 'multiple-choice-correct';
       } else if (answerKind == 'INCORRECT') {
         return 'multiple-choice-incorrect';
       } else if (answerKind == 'USER_SUBMITTED') {
         return 'input-text-correct';
       }
     };

     $scope.filterSelectable = function(answer) {
       return answer.kind == 'GOLD' ||
              answer.kind == 'INCORRECT' ||
              answer.kind == 'SILVER';
     };

     $scope.filterNotSelectable = function(answer) {
       return answer.kind == 'input-text-correct' || answer.kind == 'USER_SUBMITTED';
     };

     $scope.ranksFormating = function(userValue, totalValue) {
       var prefix = "Rank: ";
       var sufix = "---";
       if ($scope.isNormalNumber(userValue) && $scope.isNormalNumber(totalValue)) {
         var position = userValue / totalValue;
         sufix = "" + userValue + "/" + totalValue + " (Top " + $scope.toPercentage(position) + ")";
       }
       return prefix + sufix;
     };

     $scope.isNormalNumber = function(value) {
       return ! (isNaN(value) || typeof value === undefined);
     };

     $scope.toPercentage = function(fValue) {
       return (100. * fValue).toFixed(0) + "%";
     };
}]);
