angular.module('quizz').directive('slideToggle', function() {
  return {
    restrict: 'A',
    scope:{
      isOpen: "=slideToggle"
    },
    link: function(scope, element, attr) {
      scope.$watch('isOpen', function(newVal,oldVal){
        if(newVal !== oldVal){
            element.stop().slideToggle();
        }
      });
    }
  };
});