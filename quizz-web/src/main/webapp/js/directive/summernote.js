angular.module('quizz').directive('summernote', function() {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    require: '?ngModel',
    link: function($scope, element, attr, ngModel) {
      var config = {};
      var updateModel = function() {
        var newValue = element.code();
        if (ngModel && ngModel.$viewValue !== newValue) {
          if ($scope.$$phase !== '$apply' || $scope.$$phase !== '$digest' ) {
            $scope.$apply(function() {
              ngModel.$setViewValue(newValue);
            });
          }
        }
      };

      config.onkeyup = function(event) {
        updateModel();
      };

      element.summernote(config);
      
      if(ngModel) {
        ngModel.$render = function() {
          element.code(ngModel.$viewValue || '');
        };
      }

      $scope.$on('$destroy', function () {
        element.destroy();
      });

      var editor = element.next('.note-editor');
      editor.find('.note-toolbar').click(function() {
        updateModel();
      });

    }
  };
});
