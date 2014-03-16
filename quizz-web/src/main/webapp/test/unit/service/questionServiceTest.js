describe('test questionService', function() {
	var $httpBackend;
	
	beforeEach(module('quizz'));
	
	beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.when('GET', '/quizquestions/testId?num=1').respond(
            {
                id: 25,
                name: "TestName"
            }
        );
        $httpBackend.when('POST', '/processUserAnswer').respond("ok");
    }));
	
	afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

	it('list should be called', inject(['questionService', function(questionService) {
		var response;
		questionService.list(1, 'testId', function (data) {
			response = data;
        });
		$httpBackend.flush();
        expect(response).toBeDefined();
	}]));
	
	it('sendAnswer should be called', inject(['questionService', function(questionService) {
		var response;
		questionService.sendAnswer({}, function (data) {
			response = data;
        });
		$httpBackend.flush();
        expect(response).toBeDefined();
	}]));
	
});