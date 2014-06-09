describe('QuizService test', function() {
  var $httpBackend = {};
  var quizzes = {};
  var quizPerformance = {};

  beforeEach(module('quizz'));

  beforeEach(inject(function ($injector) {
    initQuizzes();
    initQuizPerformances();

    $httpBackend = $injector.get('$httpBackend');
    $httpBackend.expectGET('/listQuiz').respond(quizzes);
    $httpBackend.whenPOST('/listQuizPerformanceByUser')
                .respond(quizPerformance);
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  it('test list without caching', inject(['quizService',
    function(quizService) {
      quizService.list('user1_time1',
          function(actualQuizzes) {
            expect(actualQuizzes.length).toEqual(3);
            quizDict = {};
            for (var i = 0; i < actualQuizzes.length; i++) {
              quizDict[actualQuizzes[i]['quizID']] = actualQuizzes[i];
            }
            expect(quizDict['quiz1']).not.toBe(null);
            expect(quizDict['quiz2']).not.toBe(null);
            expect(quizDict['quiz3']).not.toBe(null);

            expect(quizDict['quiz1']['numUserAnswers']).toEqual(5);
            expect(quizDict['quiz3']['numUserAnswers']).toEqual(8);
            expect(quizDict['quiz2']['numUserAnswers']).toBeUndefined();
          },
          function() {});
      $httpBackend.flush();
    }
  ]));

  it('test list with caching', inject(['quizService',
    function(quizService) {
      // This should cache the quizzes.
      quizService.list('aff0',
          function(success) {},
          function() {});
      $httpBackend.flush();

      // The second request should then use the cached quizzes, i.e. no
      // extra call to /listQuiz.
      quizService.list('aff0',
          function(success) {},
          function() {});
      $httpBackend.flush();
    }
  ]));

  function initQuizzes() {
    quizzes = {items: [
      {quizID: 'quiz1'},
      {quizID: 'quiz2'},
      {quizID: 'quiz3'}
    ]};
  }

  function initQuizPerformances() {
    quizPerformance = {items: [
      {quiz: 'quiz1', totalanswers: 5},
      {quiz: 'quiz3', totalanswers: 8}
    ]};
  }
});
