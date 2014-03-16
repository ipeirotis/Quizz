describe('test quizService', function() {
	var $httpBackend;
	
	beforeEach(module('quizz'));
	
	beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.when('GET', '/getUserQuizPerformance').respond("ok");
        $httpBackend.when('GET', '/quizperformance/user/testUser').respond("ok");
        $httpBackend.when('GET', '/quizperformance/quiz/testQuiz/user/testUser').respond("ok");
        $httpBackend.when('GET', '/quiz').respond(
        	{"items": [
        	          {
        	        	   "name": "testName",
        	        	   "quizID": "testId"
        	          }
        	]});
    }));
	
	afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });
	
	it('list should be called', inject(['quizService', function(quizService) {
		var response;
		quizService.list('testUser', function (data) {
			response = data;
        });
		$httpBackend.flush();
        expect(response).toBeDefined();
	}]));

	it('getUserQuizPerformance should be called', inject(['quizService', function(quizService) {
		var response;
		quizService.getUserQuizPerformance('testQuiz', 'testUser', function (data) {
			response = data;
        });
		$httpBackend.flush();
        expect(response).toBeDefined();
	}]));
	
});