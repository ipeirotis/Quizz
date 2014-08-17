angular.module('quizz').controller('QuizController',
  ['$scope', '$rootScope', '$routeParams', '$location', 'questionService',
   'quizService', 'workflowService', 'userService', 'userActionService',
   function ($scope, $rootScope, $routeParams, $location, questionService,
             quizService, workflowService, userService, userActionService) {
     // Variables used in this controller and the html template.
     // The index in workflowService is 0-based but we need a 1-based number
     // to show to the user.
     $scope.currentQuestionIndex =
         workflowService.getCurrentQuestionIndex() + 1;
     $scope.numQuestions = workflowService.getNumQuestions();

     $scope.answerHelpStates = {};
     $scope.questionHelpState = true;

     $scope.toggleAnswerHelp = function(id) {
       $scope.answerHelpStates[id] = !$scope.answerHelpStates[id];
     };

     $scope.toggleQuestionHelp = function(id) {
       if ($scope.questionHelpState) {
         userActionService.recordHideQuestionContext(
             userService.getUsername(),
             $routeParams.quizId,
             workflowService.getCurrentQuestion().id,
             function(response) {},
             function(error) {}
         );
       } else {
         userActionService.recordExpandQuestionContext(
             userService.getUsername(),
             $routeParams.quizId,
             workflowService.getCurrentQuestion().id,
             function(response) {},
             function(error) {}
         );
       }
       $scope.questionHelpState = !$scope.questionHelpState;
     };

     // Fetches new list of questions from the server.
     $scope.fetchAdditionalQuestions = function() {
       questionService.list(
         $routeParams.quizId,
         $routeParams.questionId,
         userService.getUsername(),
         function(response) {
           workflowService.setQuestions(response, $routeParams.quizId);
           $scope.currentQuestion = workflowService.getNewCurrentQuestion();
           $scope.numQuestions = workflowService.getNumQuestions();
           userActionService.recordQuestionShown(
               userService.getUsername(),
               $routeParams.quizId,
               workflowService.getCurrentQuestion()?workflowService.getCurrentQuestion().id : $routeParams.questionId,
               function(response) {},
               function(error) {}
           );
           $scope.readyToShow = true;
         },
         function(error) {
         });
     };

     // Fetches the next question to be shown to the user from workflowService,
     // if possible, else get a list of new questions from the server.
     $scope.fetchQuestions = function() {
       // If we don't have questions set in the workflowService or the quizID
       // changes.
       if (!workflowService.hasQuestions() ||
           $routeParams.quizId != workflowService.getCurrentQuizID()) {
         workflowService.clear();
         $scope.currentQuestionIndex = 1;
         $scope.fetchAdditionalQuestions();
       } else if (workflowService.getNumQuestions() == -1 &&
                  !workflowService.hasEnoughQuestions()) {
         // If this is an unlimited quiz and we have not enough questions.
         workflowService.clear();
         workflowService.setCurrentQuestionIndex(
             $scope.currentQuestionIndex - 1);
         $scope.fetchAdditionalQuestions();
       } else {
         // Else, reuse the existing questions.
         $scope.currentQuestion = workflowService.getNewCurrentQuestion();
         userActionService.recordQuestionShown(
             userService.getUsername(),
             $routeParams.quizId,
             workflowService.getCurrentQuestion().id,
             function(response) {},
             function(error) {}
         );
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
           workflowService.getNumIncorrectAnswers(),
           $scope.currentQuestionIndex,
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
                 workflowService.incNumIncorrectAnswers();
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
             userService.storeCookie(response.userid);
           }
         }
         $scope.fetchQuestions();
         $scope.fetchUserQuizPerformance();
       },
       function(error) {}
     );
}]);
