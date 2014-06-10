angular.module('quizz').controller('QuizController',
  ['$scope', '$rootScope', '$routeParams', '$location', 'questionService',
   'quizService', 'workflowService', 'userService',
   function ($scope, $rootScope, $routeParams, $location, questionService,
             quizService, workflowService, userService) {
     // Variables used in this controller and the html template.
     // The index in workflowService is 0-based but we need a 1-based number
     // to show to the user.
     $scope.currentQuestionIndex =
         workflowService.getCurrentQuestionIndex() + 1;
     $scope.numQuestions = workflowService.getNumQuestions();

     // Fetches the list of questions for the associated quiz and stores them
     // in the workflow service, if necessary, then picks the next question
     // to be displayed.
     $scope.fetchQuestions = function() {
       // If we don't have questions set in the workflowService OR
       // the requested quizId is different from the quizId in the workflow
       // service, fetch a new set of questions.
       if (!workflowService.hasQuestions() ||
           $routeParams.quizId != workflowService.getCurrentQuizID()) {
         workflowService.clear();
         $scope.currentQuestionIndex = 1;
         questionService.list(
           $scope.numQuestions,
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

     // Fetches the user quiz performance and stores them in the scope.
     $scope.fetchUserQuizPerformance = function() {
       quizService.getUserQuizPerformance(
           $routeParams.quizId,
           userService.getUsername(),
           function(response) {
             $scope.performance = response;
             $scope.showPerformance = true;
           });
     };

     // Marks conversion of the given user in the quiz for the given gaType.
     // The gaType consists of quizz type (multi-choice / input-text) and
     // correctness (correct / incorrect / skip). The conversion value is
     // the average # bits of the user per answer, multiplied by 100.
     $scope.markConversion = function(quizID, username, gaType) {
       questionService.markConversion(
           quizID,
           username,
           function(response) {
             if (response && typeof(ga) != 'undefined') {
               ga('send', {
                   'hitType': 'event',
                   'hitCallback': function(){},
                   'eventCategory': 'quiz-submission',
                   'eventAction': gaType,
                   'eventLabel': quizID,
                   'eventValue': Math.round(
                       100. * response.score / response.totalanswers)
               });
             }
           },
           function(error) {
           });
     };

     // Answers the question with the given answerID or userInput if it is a
     // free text question. If the answerID == -1 and userInput is null, then
     // the user skips answering this question.
     $scope.answerQuestion = function(answerID, userInput) {
       questionService.sendAnswer(
           $routeParams.quizId,
           workflowService.getCurrentQuestion().id,
           answerID,
           userService.getUsername(),
           userInput || '',
           workflowService.getNumCorrectAnswers(),
           workflowService.getNumQuestions() -
               workflowService.getNumCorrectAnswers(),
           function(response) {
             if (response) {
               var gaType = '';
               if (userInput) {
                 gaType = 'input-text-';
               } else {
                 gaType = 'multiple-choice-';
               }

               workflowService.addUserAnswer(response.userAnswer);
               workflowService.addUserFeedback(response.userAnswerFeedback);
               workflowService.setNextQuestionGold(response.exploit);
               workflowService.updateBestAnswer(response.bestAnswer);
               workflowService.setUserAnswerId(answerID);

               if (response.userAnswerFeedback.isCorrect == true) {
                 workflowService.incNumCorrectAnswers();
                 gaType += 'correct';
               } else if (answerID == -1 && !userInput) {
                 gaType += 'skip';
               } else {
                 gaType += 'incorrect';
               }

               if (answerID != -1 || userInput) {
                 workflowService.incNumSubmittedUserAnswers();
               }
               $scope.markConversion(
                   $routeParams.quizId,
                   userService.getUsername(),
                   gaType);
             }
             $scope.showFeedback();
           },
           function(error) {
           });
     };

     $scope.showFeedback = function() {
       $location.path('/feedback');
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
       $routeParams.quizId,
       function(response) {
         if (response) {
           if (response.userid) {
             $.cookie("username", response.userid, { expires: 365, path: "/" });
           }
         }
         $scope.fetchQuestions();
         $scope.fetchUserQuizPerformance();
       },
       function(error) {}
     );
}]);
