angular.module('quizz').controller('AnswerModalController', [
    '$scope', '$modalInstance', 'answer',
    function($scope, $modalInstance, answer) {
  $scope.answer = answer || {kind: 'GOLD'};

  $scope.save = function (form) {
    if(form.$invalid){
      $scope.notValidForm = true;
      return;
    }
    $modalInstance.close($scope.answer);
  };

  $scope.close = function () {
    $modalInstance.dismiss('cancel');
  };
}]);
