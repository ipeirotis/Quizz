angular.module('quizz-admin').directive('answers', [function () {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      var tpl = '<td><strong>' + scope.question.questionText.value + '</strong></td>';

      angular.forEach(scope.question.answers, function(answer) {
        tpl += '<td>' + answer.text + '</td><td>' +
            (!answer.numberOfPicks? 0:answer.numberOfPicks) + '</td><td>' +
            (!answer.bits? 0:answer.bits.toFixed(2)) + '</td><td>' +
            (!answer.probCorrect? 0:answer.probCorrect.toFixed(2)) + '</td>';
      });
      element.html(tpl);
    }
  };
}]);
