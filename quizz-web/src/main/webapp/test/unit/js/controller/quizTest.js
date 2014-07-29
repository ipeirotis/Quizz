describe('QuizController test', function() {
  var QUIZ_1 = 'test_quiz1';
  var QUIZ_2 = 'test_quiz2';

  var userResponse = {};
  var questionsQuiz1 = {};
  var questionsQuiz2 = {};
  var quizPerformance = {};
  var routeParams = {};

  beforeEach(module('quizz'));

  beforeEach(inject(function ($injector) {
    initUserResponse();
    questionsQuiz1 = initQuestions(QUIZ_1, 10);
    questionsQuiz2 = initQuestions(QUIZ_2, -1);
    initQuizPerformance();

    $httpBackend = $injector.get('$httpBackend');
    $httpBackend.expectPOST('/getUser').respond(userResponse);
    $httpBackend.expectPOST('/listNextQuestions').respond(questionsQuiz1);
    $httpBackend.expectPOST('/getQuizPerformance').respond(quizPerformance);
    $httpBackend.expectPOST('/recordQuestionShown').respond('ok');
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
    delete $.cookie['username'];
  });

  it('test init', inject(['$rootScope', '$controller', 'userService',
    function($rootScope, $controller, userService) {
      // init scopes
      quizControllerScope = $rootScope.$new();
      // quiz controller
      routeParams['quizId'] = QUIZ_1;
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });

      // Just initialize quiz controller will call some init function to
      // create a new username, fetch question, and quiz performance.
      $httpBackend.flush();

      expect(quizControllerScope.currentQuestionIndex).toEqual(1);
      expect(quizControllerScope.numQuestions).toEqual(10);

      expect(quizControllerScope.currentQuestion.text)
          .toEqual('Calibration question 1');
      expect(quizControllerScope.currentQuestion.quizID).toEqual(QUIZ_1);
      expect(quizControllerScope.readyToShow).toEqual(true);

      expect(quizControllerScope.performance.score).toEqual(0.7891);
      expect(quizControllerScope.showPerformance).toEqual(true);
    }])
  );

  it('test reload diff quiz', inject([
    '$rootScope', '$controller', 'userService',
    function($rootScope, $controller, userService) {
      // Mocks the storeCookie function to store a cookie that works without
      // https since karma test starts a http server.
      userService.storeCookie = function(userid) {
        $.cookie("username", userid);
      };

      // init scopes
      quizControllerScope = $rootScope.$new();
      // quiz controller
      routeParams['quizId'] = QUIZ_1;
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });

      // Init with first quiz.
      $httpBackend.flush();

      // Switch to second quiz.
      routeParams['quizId'] = QUIZ_2;
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });

      // As the routeParams changes, we need to fetch new questions.
      $httpBackend.expectPOST('/listNextQuestions').respond(questionsQuiz2);
      $httpBackend.expectPOST('/getQuizPerformance').respond(quizPerformance);
      $httpBackend.expectPOST('/recordQuestionShown').respond('ok');
      $httpBackend.flush();
      expect(quizControllerScope.currentQuestion.quizID).toEqual(QUIZ_2);
    }])
  );

  it('test reload same quiz', inject([
    '$rootScope', '$controller', 'userService',
    function($rootScope, $controller, userService) {
      // Mocks the storeCookie function to store a cookie that works without
      // https since karma test starts a http server.
      userService.storeCookie = function(userid) {
        $.cookie("username", userid);
      };

      // init scopes
      quizControllerScope = $rootScope.$new();
      // quiz controller
      routeParams['quizId'] = QUIZ_1;
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });

      // Init with first quiz.
      $httpBackend.flush();

      // Reload again with the same quiz.
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });

      // Since this is the same quiz, we won't need to fetch questions again.
      $httpBackend.expectPOST('/recordQuestionShown').respond('ok');
      $httpBackend.expectPOST('/getQuizPerformance').respond(quizPerformance);
      $httpBackend.flush();
      expect(quizControllerScope.currentQuestion.quizID).toEqual(QUIZ_1);
    }])
  );

  it('test fetch unlimited question', inject([
    '$rootScope', '$controller', 'userService', 'workflowService',
    function($rootScope, $controller, userService, workflowService) {
      // Mocks the storeCookie function to store a cookie that works without
      // https since karma test starts a http server.
      userService.storeCookie = function(userid) {
        $.cookie("username", userid);
      };

      // Sets up listNextQuestions to only return 2 gold questions for the
      // "unlimited" quiz.
      questionsQuiz2.calibration = questionsQuiz2.calibration.slice(0, 2);
      questionsQuiz2.collection = [];
      $httpBackend.resetExpectations();
      $httpBackend.expectPOST('/getUser').respond(userResponse);
      $httpBackend.expectPOST('/listNextQuestions').respond(questionsQuiz2);
      $httpBackend.expectPOST('/getQuizPerformance').respond(quizPerformance);
      $httpBackend.expectPOST('/recordQuestionShown').respond('ok');

      quizControllerScope = $rootScope.$new();
      routeParams['quizId'] = QUIZ_2;
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });
      $httpBackend.flush();
      expect(quizControllerScope.numQuestions).toEqual(-1);

      // Simulate answering the first question.
      workflowService.incCurrentQuestionIndex();
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });
      // No extra call to fetch additional question.
      $httpBackend.expectPOST('/recordQuestionShown').respond('ok');
      $httpBackend.expectPOST('/getQuizPerformance').respond(quizPerformance);
      $httpBackend.flush();

      // Simulate answering the second question.
      workflowService.incCurrentQuestionIndex();
      $controller('QuizController', {
        $scope: quizControllerScope, $routeParams: routeParams
      });
      // Since there is only two questions in this unlimited QUIZ_2, we need to
      // fetch questions again.
      $httpBackend.expectPOST('/listNextQuestions').respond(questionsQuiz2);
      $httpBackend.expectPOST('/getQuizPerformance').respond(quizPerformance);
      $httpBackend.expectPOST('/recordQuestionShown').respond('ok');
      $httpBackend.flush();
    }])
  );

  function initUserResponse() {
    userResponse = {
      userid: 'aff2',
    };
  }

  function initQuestions(quizID, numQuestions) {
    var calibrationQuestions = [];
    for (var i = 1; i <= 10; i++) {
      var question = {
        'quizID' : quizID,
        'text' : 'Calibration question ' + i,
        'answers': []
      };
      for (var j = 1; j < 4; j++) {
        question.answers.push({
          'internalID' : j,
          'text' : 'Calibration question ' + i + ' answer ' + j,
          'kind' : i % 4 == j ? 'GOLD' : 'INCORRECT',
          'quizID' : quizID
        });
      }
      calibrationQuestions.push(question);
    }

    var collectionQuestions = [];
    for (var i = 1; i <= 10; i++) {
      var question = {
        'quizID' : quizID,
        'text' : 'Collection question ' + i,
        'answers': []
      };
      for (var j = 1; j < 4; j++) {
        question.answers.push({
          'internalID' : j,
          'text' : 'Collection question ' + i + ' answer ' + j,
          'kind' : 'SILVER',
          'quizID' : quizID
        });
      }
      collectionQuestions.push(question);
    }

    return {
      calibration: calibrationQuestions,
      collection: collectionQuestions,
      numQuestions: numQuestions
    };
  }

  function initQuizPerformance() {
    quizPerformance = {
      score: 0.7891,
      rankScore: 3,
      totalUsers: 10,
      correctanswers: 4,
      totalanswers: 8,
      percentageCorrect: 0.5
    };
  }
});
