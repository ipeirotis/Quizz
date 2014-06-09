describe('ListController test', function() {
  var userResponse = {};
  var quizPerformance = {};
  var quizzes = {};

  beforeEach(module('quizz'));

  beforeEach(inject(function ($injector) {
    initUserResponse();
    initQuizPerformance();
    initQuizzes();

    $httpBackend = $injector.get('$httpBackend');

    $httpBackend.expectPOST('/getUser').respond(userResponse);
    $httpBackend.expectPOST('/listQuizPerformanceByUser')
                .respond(quizPerformance);
    $httpBackend.expectGET('/listQuiz').respond(quizzes);
  }));

  afterEach(function() {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
    delete $.cookie['username'];
  });

  it('list controller test', inject(['$rootScope', '$controller', 'userService',
    function($rootScope, $controller, userService) {
      // init scopes
      listControllerScope = $rootScope.$new();
      // list controller
      $controller('ListController', {$scope: listControllerScope});

      // Just initialize list controller will call some init function
      // that sets up the list of quizzes and gets a new username cookie.
      $httpBackend.flush();

      expect(listControllerScope.quizes.length).toEqual(3);
      expect(listControllerScope.readyToShow).toEqual(true);
      expect(userService.getUsername()).toEqual('aff1');
    }])
  );

  function initUserResponse() {
    userResponse = {
      userid: 'aff1',
    };
  }

  function initQuizPerformance() {
    quizPerformance = {items: [
      {quiz: 'quiz1', totalanswers: 5},
      {quiz: 'quiz3', totalanswers: 8}
    ]};
  }

  function initQuizzes() {
    quizzes = {items: [
      {quizID: 'quiz1'},
      {quizID: 'quiz2'},
      {quizID: 'quiz3'}
    ]};
  }
});
