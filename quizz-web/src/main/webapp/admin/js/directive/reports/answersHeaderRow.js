angular.module('quizz-admin').directive('answersheader', [function () {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {

			scope.$watch('reportData', function(newValue, oldValue) {
				var tpl = '<th>Question</th>';
				
				if(scope.reportData && scope.reportData.length > 0){
					var count = 0;
					for(var i=0;i<Math.min(5,scope.reportData.length);i++){
						if(scope.reportData[i].answers.length > count)
							count = scope.reportData[i].answers.length;
					}
					for(var j=0;j<count;j++){
						tpl += '<th>Answer</th><th>Picks</th><th>Bits</th><th>probCorrect</th>';
					}
						element.html(tpl);
				}
			});
		}
	};
}]);