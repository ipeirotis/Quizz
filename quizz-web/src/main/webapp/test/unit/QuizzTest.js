describe('full test', function() {
  var TEST_QUIZ_ID = 'testQuizId';
  var TEST_QUIZ_NAME = 'TestQuizName';
  var TEST_QUIZ = {
    'name' : TEST_QUIZ_NAME,
    'quizID' : TEST_QUIZ_ID,
    'questions' : 10,
    'gold' : 10
  };
  var RESPONSE_ON_ANSWER = {
    'userAnswer': {},
    'userAnswerFeedback': {
      'isCorrect': true
    }
  };
  var $httpBackend = {};
  var listControllerScope, quizControllerScope, feedbackControllerScope,
      summaryControllerScope;
  var routeParams = {};
  var questions = [];

  beforeEach(module('quizz'));

  beforeEach(inject(function ($injector) {
    $httpBackend = $injector.get('$httpBackend');
    $httpBackend.when('GET', '/listQuiz').respond({"items": [TEST_QUIZ]});
    $httpBackend.when('POST', '/listQuizPerformanceByUser').respond("ok");
    $httpBackend.when('POST', '/getQuizPerformance').respond("ok");
    $httpBackend.when('POST', '/getUser').respond("ok");
    $httpBackend.when('POST', '/listNextQuestions')
                .respond({"calibration": questions});
    $httpBackend.when('POST', '/processUserAnswer')
                .respond(RESPONSE_ON_ANSWER);
    initQuestions();
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  it('Quizz test', inject(['$rootScope', '$controller', 'workflowService',
    function($rootScope, $controller, workflowService) {
      // init scopes
      listControllerScope = $rootScope.$new();
      feedbackControllerScope = $rootScope.$new();
      summaryControllerScope = $rootScope.$new();
      quizControllerScope = $rootScope.$new();

      // list controller
      $controller('ListController', {$scope: listControllerScope});

      // quiz controller
      routeParams['quizId'] = TEST_QUIZ_ID;
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams : routeParams
      });

      $httpBackend.flush();

      // check the number of quizzes, should be 1
      expect(listControllerScope.quizes.length).toEqual(1);
      // check the number of calibration questions, should be 10
      expect(workflowService.getQuestions().calibration.length).toEqual(10);

      // check all questions
      for (var i = 0; i < workflowService.getQuestions().calibration.length;
           i++) {
        quizControllerScope.answerQuestion(1);
        $httpBackend.flush();
        expect(workflowService.getUserFeedbacks().length).toEqual(i + 1);
        expect(workflowService.getNumCorrectAnswers()).toEqual(i + 1);

        // check feedbacks
        $controller('FeedbackController', {$scope: feedbackControllerScope});
        expect(feedbackControllerScope.feedback.isCorrect).toEqual(true);
      }

      // check summary info
      $controller('SummaryController', {$scope: summaryControllerScope});
      expect(summaryControllerScope.correctAnswersCount).toEqual(10);

      // check that workflowService is clean
      summaryControllerScope.startAgain();
      expect(workflowService.getUserFeedbacks().length).toEqual(0);
    }])
  );

  // Initializes the global questions variable to contain 10 questions, each
  // of which has 4 answers each.
  function initQuestions() {
    for (var i = 1; i <= 10; i++) {
      var question = {
        'quizID' : TEST_QUIZ_ID,
        'weight' : 1,
        'text' : 'Test question' + i,
        'answers': []
      };
      for (var j = 1; j < 4; j++) {
        question.answers.push({
          'internalID' : j,
          'text' : 'Test question ' + i + ' answer ' + j,
          'kind' : j == 1 ? 'GOLD' : 'INCORRECT',
          'quizID' : TEST_QUIZ_ID
        });
      }
      questions.push(question);
    }
  }
});
