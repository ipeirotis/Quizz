describe('test utils', function() {
	beforeEach(module('quizz'));

	it('should return true', inject(['utils',function(utils) {
		var valid = utils.isNormalNumber(11);
		expect(valid).toBeTruthy();
	}]));
	
	it('should return true', inject(['utils',function(utils) {
		valid = utils.isNormalNumber('abc');
		expect(valid).toBeFalsy();
	}]));

});