angular.module('quizz').controller('QuizController',
  ['$scope', '$rootScope', '$routeParams', '$location', 'questionService',
   'quizService', 'workflowService', 'userService',
   function ($scope, $rootScope, $routeParams, $location, questionService,
             quizService, workflowService, userService) {
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

     $scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
     $scope.numOfQuestions = workflowService.getNumOfQuestions();
     $rootScope.$on("event:channel", function (event, data) {
       $scope.performance = data;
     });

     $scope.fetchQuestions = function() {
       // If we don't have questions set in the workflowService OR
       // the requested quizId is different from the quizId in the workflow
       // service, fetch a new set of questions.
       if (!workflowService.hasQuestions() ||
           $routeParams.quizId != workflowService.getCurrentQuizID()) {
         workflowService.clear();
         $scope.currentQuestionIndex = 1;
         questionService.list(
           $scope.numOfQuestions,
           $routeParams.quizId,
           userService.getUsername(),
           function(response) {
             workflowService.setQuestions(response, $routeParams.quizId);
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

     $scope.fetchUserQuizPerformance = function() {
       quizService.getUserQuizPerformance($routeParams.quizId, userService.getUsername(),
         function(response) {
           $scope.performance = response;
           $scope.showPerformance = true;
         });
     };

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
           if (response.userAnswerFeedback.isCorrect == true) {
             workflowService.incNumOfCorrectAnswers();
           }
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

     $scope.getGaType = function(answer) {
       if (answer.kind == 'GOLD') {
         return 'multiple-choice-correct';
       } else if (answer.kind == 'SILVER') {
         if (answer.probability >= 0.5) {
           return 'multiple-choice-correct';
         } else {
           return 'multiple-choice-incorrect';
         }
       } else if (answer.kind == 'INCORRECT') {
         return 'multiple-choice-incorrect';
       } else if (answer.kind == 'USER_SUBMITTED') {
         return 'input-text-correct';
       }
     };

     $scope.filterSelectable = function(answer) {
       return answer.kind == 'GOLD' ||
              answer.kind == 'INCORRECT' ||
              answer.kind == 'SILVER';
     };

     $scope.filterNotSelectable = function(answer) {
       return answer.kind == 'USER_SUBMITTED';
     };

     $scope.ranksFormating = function(userValue, totalValue) {
       var prefix = "Rank: ";
       var sufix = "---";
       if ($scope.isNormalNumber(userValue) && $scope.isNormalNumber(totalValue)) {
         var position = userValue / totalValue;
         sufix = "" + userValue + "/" + totalValue + " (Top " +
             $scope.toPercentage(position) + ")";
       }
       return prefix + sufix;
     };

     $scope.isNormalNumber = function(value) {
       return ! (isNaN(value) || typeof value === undefined);
     };

     $scope.toPercentage = function(fValue) {
       return (100. * fValue).toFixed(0) + "%";
     };

     // Gets or creates a new user id if user directly arrives at the question
     // page.
     userService.maybeCreateUser(
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
         $scope.fetchQuestions();
         $scope.fetchUserQuizPerformance();
       },
       function(error) {}
     );
}]);
