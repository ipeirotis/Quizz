angular.module('quizz-admin').directive('navbar', ['$location',
  function ($location) {
    return {
      restrict: 'A',
      link: function postLink(scope, element, attrs, controller) {
        scope.$watch(function () {
          return $location.path();
        }, function (newValue, oldValue) {
          $('li[data-match-route]', element).each(function (k, li) {
            var $li = angular.element(li);
            var pattern = $li.attr('data-match-route');
            var regexp = new RegExp('^' + pattern + '$', ['i']);
            if (regexp.test(newValue)) {
              $li.addClass('active');
            } else {
              $li.removeClass('active');
            }
          });
        });
      }
    };
  }
]);
