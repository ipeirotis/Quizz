angular.module('quizz-admin', ['ngRoute', 'ngSanitize'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/quizzes', {templateUrl: templates.quizzes, controller: 'QuizzesController'})
	.when('/report', {templateUrl: templates.report, controller: 'ReportController', reloadOnSearch:false})
	.otherwise({redirectTo: '/report'});
}])

.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push('interceptor');
}]);angular.module('quizz-admin').controller('MenuController', 
	['$scope', '$location',	function ($scope, $location) {
	
	$scope.isActive = function (view) {
		if($location.path().indexOf(view) == 0 ){
			return true;
		} else {
			return false;
		}
    };
    
}]);angular.module('quizz-admin').controller('QuizzesController', 
	['$scope', function ($scope) {
	
}]);angular.module('quizz-admin').controller('ReportController', 
	['$scope', '$rootScope', '$routeParams', '$location', 'reportService',
	 function ($scope, $rootScope, $routeParams, $location, reportService) {
		
	$scope.quizID = $routeParams.quizId;
	
	$scope.$watch('quizID', function(newValue, oldValue) {console.log(newValue)
		if(newValue && newValue != ''){
			$scope.load(newValue);
			$location.search('quizId', newValue);
			$routeParams.quizId = newValue;
		}else{
			$scope.reportData = [];
			$location.search('quizId', null);
			$routeParams.quizId = null;
		}
	});
	
	$scope.loadQuizes = function(){
		reportService.listQuizes(
			function(response) {
				$scope.quizes = response.items;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.loadQuizes();
	
	$scope.load = function(quizID){
		reportService.load($scope.quizID,
			function(response) {
				$scope.reportData = response.items;
			},
			function(error) {
		});
	};
	
}]);angular.module('quizz-admin').directive('navbar', ['$location',
	function ($location) {
	  return {
	    restrict: 'A',
	    link: function postLink(scope, element, attrs, controller) {
	      scope.$watch(function () {
	        return $location.path();
	      }, function (newValue, oldValue) {
	        $('li[data-match-route]', element).each(function (k, li) {
	          var $li = angular.element(li), pattern = $li.attr('data-match-route'), regexp = new RegExp('^' + pattern + '$', ['i']);
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
]);angular.module('quizz-admin').directive('answersheader', [function () {
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
						tpl += '<th>Answer</th><th>Picks</th><th>Bits</th>';
					}
						element.html(tpl);
				}
			});
		}
	};
}]);angular.module('quizz-admin').directive('answers', [function () {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var tpl = '<td><strong>' + scope.question.text + '</strong></td>';
			
			angular.forEach(scope.question.answers, function(answer){
			    tpl += '<td>' + answer.text + '</td><td>' 
			    	+ (!answer.numberOfPicks? 0:answer.numberOfPicks) + '</td><td>'
			    	+ (!answer.bits? 0:answer.bits.toFixed(2)) + '</td>';
			});
			element.html(tpl);
		}
	};
}]);angular.module('quizz-admin').factory('interceptor', 
	['$rootScope', '$q', '$location', 'loading', function($rootScope, $q, $location, loading) {
		
	return {
		request: function(config) {
			loading.show();
		    return config || $q.when(config);
		},
	    response: function(response) {
	    	loading.hide();
	    	return response || $q.when(response);
	    },
	    responseError: function(rejection) {
	    	loading.hide();
	    	return $q.reject(rejection);
	    }
  	};
}]);angular.module('quizz-admin').factory('loading', function(){
	var service = {
	    requestCount: 0,
	    message: $('<div id="loading"/>'),
	    show: function() {
	      this.requestCount++;
	      var width = $(window).width();
    
	      if($("#loading").length === 0){
	    	  this.message.appendTo($(document.body));
	      }
	      
	      this.message
	      .addClass('notification')
	      .addClass('inf')
	      .css('left', width/2-this.message.width()/2)
	      .text('Loading ...')	      
	      .show();
	    },
	    hide: function() {
	       this.requestCount--;
		   if( this.requestCount === 0 ){
		   	  this.message.hide();
		   }
		},
	    isLoading: function() {
	      return this.requestCount > 0;
	    }
	};
	return service;
});angular.module('quizz-admin').factory('reportService', ['$http', function($http){
    return {
    	load: function(quizId, success, error) {
    		var url = Config.api + '/report?quizID=' + quizId;
        	$http.get(url).success(success).error(error);
        },
    	listQuizes: function(success, error) {
        	$http.get(Config.api + '/quiz').success(success).error(error);
        }
    };
}]);angular.module('quizz-admin').constant('templates', {
	quizzes: 'views/quizzes.html?v=1',
	report: 'views/report.html?v=1'
});