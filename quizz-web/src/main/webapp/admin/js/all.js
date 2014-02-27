angular.module('quizz-admin', ['ngRoute', 'ngSanitize'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/quizzes', {templateUrl: templates.quizzes, controller: 'QuizzesController'})
	.when('/reports/answers', {templateUrl: templates.answersReport, controller: 'AnswersReportController', reloadOnSearch:false})
	.when('/reports/scoreByBrowser', {templateUrl: templates.scoreByBrowserReport, controller: 'ScoreByBrowserReportController', reloadOnSearch:false})
	.when('/reports/scoreByDomain', {templateUrl: templates.scoreByDomainReport, controller: 'ScoreByDomainReportController', reloadOnSearch:false})
	.when('/reports/contributionQuality', {templateUrl: templates.contributionQualityReport, controller: 'ContributionQualityReportController', reloadOnSearch:false})
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
	
}]);angular.module('quizz-admin').controller('AnswersReportController', 
	['$scope', '$rootScope', '$routeParams', '$location', 'reportService',
	 function ($scope, $rootScope, $routeParams, $location, reportService) {
		
	$scope.quizID = $routeParams.quizId;
	
	$scope.$watch('quizID', function(newValue, oldValue) {
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
		reportService.loadAnswersReport($scope.quizID,
			function(response) {
				$scope.answersKind = $scope.getAnswersKind(response.items);
				$scope.reportData = response.items;
			},
			function(error) {
		});
	};
	
	$scope.getAnswersKind = function(items){
		var result = '';
       	angular.forEach(items, function(item){	
			if(item.answers && item.answers.length > 0){
				var kind = item.answers[0].kind;
				if(kind == 'selectable_not_gold' || kind == 'selectable_gold'){
					result = 'SELECTABLE';
				}else{
					result = 'INPUT';
				}
				return;
			}
		});
       	return result;
	};
	
}]);angular.module('quizz-admin').controller('ContributionQualityReportController', 
	['$scope', 'reportService', function ($scope, reportService) {
	
	$scope.load = function(){
		reportService.loadContributionQualityReport(
			function(response) {
				$scope.reportData = response.items;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.load();
	
	$scope.toPercentage = function(value) {
		return (100. * value).toFixed(0) + "%";
	};
	
}]);angular.module('quizz-admin').controller('ScoreByBrowserReportController', 
	['$scope', 'reportService', function($scope, reportService) {
		
	$scope.load = function(){
		reportService.loadScoreByBrowserReport(
			function(response) {
				$scope.reportData = response.items;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.load();
		
}]);angular.module('quizz-admin').controller('ScoreByDomainReportController', 
	['$scope', 'reportService', function($scope, reportService) {
	
	$scope.pageNumber = 0;
	
	$scope.load = function(){
		reportService.loadScoreByDomainReport($scope.pageNumber,
			function(response) {
				$scope.reportData = response;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.load();
	
	$scope.nextPage = function(){
		$scope.pageNumber++;
		$scope.load();
	};
	
	$scope.prevPage = function(){
		if($scope.pageNumber > 0){
			$scope.pageNumber--;
			$scope.load();
		}
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
});angular.module('quizz-admin').factory('reportService', 
		['$http', '$cacheFactory', function($http, $cacheFactory){
	var LIMIT = 20;
	var pageTokens = [];
	var cache = $cacheFactory('domainsReportCache');
	
    return {
    	loadAnswersReport: function(quizId, success, error) {
    		var url = Config.api + '/reports/answers?quizID=' + quizId;
        	$http.get(url).success(success).error(error);
        },
    	loadScoreByBrowserReport: function(success, error) {
    		var url = Config.api + '/reports/scoreByBrowser';
        	$http.get(url).success(success).error(error);
        },
    	loadScoreByDomainReport: function(pageNumber, success, error) {
    		if(pageNumber > 0 && pageNumber < pageTokens.length){
    			var items = cache.get(pageTokens[pageNumber]);
	        	if(items && angular.isFunction(success)){
	        		success(items);
	        		return;
	        	}
    		}
    		var url = Config.api + '/reports/scoreByDomain?limit=' + LIMIT;
    		if(pageNumber > 0){
    			console.log(pageTokens.length);
    			url += '&cursor=' + pageTokens[pageNumber-1];
    		}
        	$http.get(url).success(function(response) {
				if(response.nextPageToken){
					pageTokens.push(response.nextPageToken);
					cache.put(pageTokens[pageNumber], response.items);
				}
	        	if(angular.isFunction(success)){
	        		success(response.items);
	        	}
	        }).error(error);
        },
    	loadContributionQualityReport: function(success, error) {
    		var url = Config.api + '/reports/contributionQuality';
        	$http.get(url).success(success).error(error);
        },
    	listQuizes: function(success, error) {
        	$http.get(Config.api + '/quiz').success(success).error(error);
        }
    };
}]);angular.module('quizz-admin').constant('templates', {
	quizzes: 'views/quizzes.html?v=1',
	answersReport: 'views/reports/answersReport.html?v=1',
	scoreByBrowserReport: 'views/reports/scoreByBrowserReport.html?v=1',
	scoreByDomainReport: 'views/reports/scoreByDomainReport.html?v=1',
	contributionQualityReport: 'views/reports/contributionQualityReport.html?v=1'
});