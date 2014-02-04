angular.module('quizz', ['ngRoute'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/feedback', {templateUrl: templates.feedback, controller: 'FeedbackController'})
	.when('/list', {templateUrl: templates.list, controller: 'ListController'})
	.when('/quiz', {templateUrl: templates.quiz, controller: 'QuizController', reloadOnSearch:false})
	.when('/summary', {templateUrl: templates.summary, controller: 'SummaryController'})
	.otherwise({redirectTo: '/list'});
}])

.config(['$httpProvider', function($httpProvider) {
	$httpProvider.responseInterceptors.push('interceptor');
}])

.run(['$rootScope', 'utils', 
    function($rootScope, utils) {
	$rootScope.utils = utils;
}]);angular.module('quizz').controller('FeedbackController', 
		['$scope', '$routeParams', '$location', '$q', '$modal', 'questionService', 'workflowService',
		 function ($scope, $routeParams, $location, $q, $modal, questionService, workflowService) {

	$scope.feedback = workflowService.getLastFeedback();
	$scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
	$scope.numOfQuestions = workflowService.getNumOfQuestions();	
	
	$scope.challengeAnswer = function() {
		var modalPromise = $modal({template: '/views/modals/challengeAnswer.html', 
			persist: false, show: false, keyboard: true, 
			data: {}});
		
		$q.when(modalPromise).then(function(modalEl) {
		    modalEl.modal('show');
		});
	};
	
	$scope.nextQuestion = function(){
		workflowService.incCurrentQuestionIndex();
		
		if(workflowService.getCurrentQuestionIndex() < workflowService.getNumOfQuestions()){
			$location.path('/quiz');
		} else {
			$location.path('/summary');
		}
	};
	
}]);angular.module('quizz').controller('ListController', 
	['$scope', 'quizService', 'userService', function ($scope, quizService, userService) {
		
	$scope.fetchQuizes = function(){
		quizService.list(userService.getUsername(),
			function(response) {
				$scope.quizes = response;
				$scope.readyToShow = true;
			},
			function(error) {
		});
	};
	
	$scope.fetchQuizes();
	
}]);angular.module('quizz').controller('ChallengeAnswerController', 
	['$scope', '$routeParams', 'questionService', 'workflowService', 
	 function ($scope, $routeParams, questionService, workflowService) {
	
	$scope.send = function(){
		var params = {
				quizID: $routeParams.quizId,
				questionID: workflowService.getCurrentQuestion().id,
				userAnswerID: workflowService.getLastAnswer().key.id,
				userid: workflowService.getLastFeedback().userid,
				message: $scope.challengeAnswerMessage
			};
			questionService.challengeAnswer(params,
				function(response) {
					$scope.hide();			
				},
				function(error) {
		});
	};
}]);angular.module('quizz').controller('QuizController', 
	['$scope', '$routeParams', '$location', 'questionService', 'quizService', 'workflowService', 'userService',
	 function ($scope, $routeParams, $location, questionService, quizService, workflowService, userService) {
		
		$scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
		$scope.numOfQuestions = workflowService.getNumOfQuestions();
		
		$scope.fetchQuestions = function(){
			questionService.list($scope.numOfQuestions, $routeParams.quizId,
				function(response) {
					workflowService.setQuestions(response);
					
					$scope.currentQuestion = workflowService.getCurrentQuestion();
					$scope.readyToShow = true;
				},
				function(error) {
			});
		};
		
		$scope.fetchQuestions();
		
		$scope.fetchUserQuizPerformance = function(){
			quizService.getUserQuizPerformance($routeParams.quizId, userService.getUsername(),
				function(response) {
				$scope.performance = response;
				$scope.showPerformance = true;
			});
		};
		
		$scope.fetchUserQuizPerformance();
		
		$scope.answerQuestion = function(answerID, gaType, userInput){			
			var params = {
				quizID: $routeParams.quizId,
				questionID: workflowService.getCurrentQuestion().id,
				answerID: answerID,
				userInput: userInput || '',
				totalanswers: 0,//TODO
				correctanswers: 0,
				a: 0,
				b: 0,
				c: 0
			};
			questionService.sendAnswer(params,
				function(response) {
					workflowService.addUserAnswer(response.userAnswer);
					workflowService.addUserFeedback(response.userAnswerFeedback);
					$scope.showFeedback();
				},
				function(error) {
			});
			
			questionService.markConversion(gaType, $routeParams.quizId,
					userService.getUsername());
		};
		
		$scope.showFeedback = function(){
			$location.path('/feedback');
		};
		
		$scope.getGaType = function(answerKind){
			if(answerKind == 'selectable_gold'){
				return 'multiple-choice-correct';
			}else if(answerKind == 'selectable_not_gold'){
				return 'multiple-choice-incorrect';
			}else if(answerKind == 'input_text'){
				return 'input-text-correct';
			}
		};
		
		$scope.filterSelectable = function(answer) {
	        return answer.kind == 'selectable_gold'
	        	|| answer.kind == 'selectable_not_gold';
	    };
	    
		$scope.filterNotSelectable = function(answer) {
	        return answer.kind == 'input-text-correct';
	    };

	    $scope.ranksFormating = function(userValue, totalValue) {
			var prefix = "Rank: ";
			var sufix = "---";
			if ($scope.isNormalNumber(userValue) && $scope.isNormalNumber(totalValue)) {
				var position = userValue / totalValue;
				sufix = "" + userValue + "/" + totalValue + " (Top " + $scope.toPercentage(position) + ")";
			}
			return prefix + sufix;
		};
		
		$scope.isNormalNumber = function(value) {
			return ! (isNaN(value) || typeof value === undefined);
		};
		
		$scope.toPercentage = function(fValue) {
			return (100. * fValue).toFixed(0) + "%";
		};
		
}]);angular.module('quizz').controller('SummaryController', 
	['$scope', '$routeParams', '$location', 'workflowService', 
	 function ($scope, $routeParams, $location, workflowService) {
	
	$scope.numOfQuestions = workflowService.getNumOfQuestions();
	
	$scope.getCorrectAnswersCount = function(){
		var count = 0;
       	angular.forEach(workflowService.getUserFeedbacks(), function(feedback){
       		if(feedback.isCorrect)
       			count++;
		});
       	
       	return count;
	};
	
	$scope.startAgain = function(){
		workflowService.clear();
		$location.path('/quiz');
	};
}]);angular.module('quizz').factory('interceptor', 
	['$rootScope', '$q', '$location', 'loading', function($rootScope, $q, $location, loading){
	return function(promise) {
		loading.show();
	
		return promise.then(
			function (response) {
				loading.hide();
				return response;
			}, 
			function (response) {
				if (response.status == 401) {
			        var deferred = $q.defer();
			        if($rootScope.url401 == ''){
			        	$rootScope.url401 = $location.url();
			        }
					$location.path("/login");
					loading.hide();
					return deferred.promise;
				}
				loading.hide();
				return $q.reject(response);
		});
	};
}]);angular.module('quizz').factory('loading', function(){
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
});angular.module('quizz').factory('$modal', 
	['$rootScope', '$compile', '$http', '$timeout', '$q', '$templateCache',
	function ($rootScope, $compile, $http, $timeout, $q, $templateCache) {
	var ModalFactory = function ModalFactory(config) {
	      function Modal(config) {
	        var options = angular.extend({ show: true }, config);
	        var scope = options.scope ? options.scope : $rootScope.$new();
	        var templateUrl = options.template;
	        return $q.when($templateCache.get(templateUrl) || $http.get(templateUrl, { cache: true }).then(function (res) {
	          return res.data;
	        })).then(function onSuccess(template) {
	          var id = templateUrl.replace('.html', '').replace(/[\/|\.|:]/g, '-') + '-' + scope.$id;
	          var $modal = $('<div class="modal" tabindex="-1"></div>').attr('role', 'dialog')
	          	.attr('aria-hidden', true).attr('id', id).html(template);
	          if (options.modalClass)
	            $modal.addClass(options.modalClass);
	          $('body').append($modal);
	          $timeout(function () {
	            $compile($modal)(scope);
	          });
	          scope.$modal = function (name) {
	            $modal.modal(name);
	          };
	          angular.forEach([
	            'show',
	            'hide'
	          ], function (name) {
	            scope[name] = function () {
	              $modal.modal(name);
	            };
	          });
	          scope.dismiss = scope.hide;
	          angular.forEach([
	            'show.bs.modal',
	            'shown.bs.modal',
	            'hide.bs.modal',
	            'hidden.bs.modal'
	          ], function (name) {
	            $modal.on(name, function (ev) {
	              scope.$emit(/*'modal-' + */name, ev);
	            });
	          });
	          if(options.data){
	        	  scope['data'] = angular.copy(options.data);
	          }
	          $modal.on('shown.bs.modal', function (ev) {
	            $('input[autofocus], textarea[autofocus]', $modal).first().trigger('focus');
	          });
	          $modal.on('hidden.bs.modal', function (ev) {
	            if (!options.persist)
	              scope.$destroy();
	          });
	          scope.$on('$destroy', function () {
	            $modal.remove();
	          });
	          $modal.modal(options);
	          return $modal;
	        });
	      }
	      return new Modal(config);
	    };
	    return ModalFactory;
}]);angular.module('quizz').factory('questionService', ['$http', function($http){
	var options = {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
	
    return {
    	list: function(numOfQuestions, quizId, success, error) {
    		var url = Config.api + '/quizquestions/' + quizId + '?num=' + numOfQuestions;
        	$http.get(url).success(success).error(error);
        },
        sendAnswer: function(params, success, error){
        	$http.post(Config.api + '/processUserAnswer', $.param(params), options)
        		.success(success).error(error);
        },
        markConversion: function(type, quizId, username){
			var url = Config.api + '/quizperformance';
			url += '/quiz/' + quizId;
			url += '/user/' + username;
			
        	$http.get(url).success(function(response){
        		ga('send', {
	    			  'hitType': 'event',
	    			  'hitCallback': function(){ },
	    			  'eventCategory': 'quiz-submission',
	    			  'eventAction': type,
	    			  'eventLabel': quizId,
	    			  'eventValue': 1.0*response.score/response.totalanswers,
	    			  } );
        	});
        },
        challengeAnswer: function(params, success, error){
        	$http.post(Config.api + '/addAnswerChallenge', $.param(params), options)
        		.success(success).error(error);
        }
    };
}]);angular.module('quizz').factory('quizService', 
	['$http', '$q', '$rootScope', '$cacheFactory', function($http, $q, $rootScope, $cacheFactory){

	var keys = [];
	var cache = $cacheFactory('quizCache');
	
    return {
    	list: function(user, success, error) {
    		var self = this;
    		var userQuizPerformancesRequest = 
    			$http.get(Config.api + '/quizperformance/user/' + encodeURIComponent(user));

    		var quizRequest;
    			
    		if(keys.length == 0) {
    			quizRequest = $http.get(Config.api + '/quiz');
    		} else {
    			quizRequest = $q.when(this.getQuizesFromCache());
    		}
    		
    		$q.all([quizRequest, userQuizPerformancesRequest]).then(function(arrayOfResults) {
    			var quizes = arrayOfResults[0].data.items;
    			var userQuizPerformances = arrayOfResults[1].data.items;
    			
    	       	angular.forEach(userQuizPerformances, function(userQuizPerformance){
        	       	angular.forEach(quizes, function(quiz){
        	       		if(quiz.quizID == userQuizPerformance.quiz){
        	       			quiz['totalanswers'] = userQuizPerformance.totalanswers;
        	       		}
        			});
    			});
    			
    			self.cacheQuizes(quizes);
    			
        		if(angular.isFunction(success)){
        			success(quizes);
        		}
    		});
        },
        getUserQuizPerformance: function(quiz, user, success, error){
        	var url = Config.api + '/quizperformance';
    		url += '/quiz/' + encodeURIComponent(quiz);
    		url += '/user/' + encodeURIComponent(user);
    		
    		$http.get(url).success(success).error(error);
        },
        cacheQuizes: function(quizes){
        	keys = [];
	       	angular.forEach(quizes, function(value, key){
				cache.put(value.quizID, value);
				keys.push(value.quizID);
			});
        },
        getQuizesFromCache: function(){
			var result = [];
			angular.forEach(keys, function(value, key){
				result.push(cache.get(value));
			});
			return {data:{items: result}};
        }
    };
}]);angular.module('quizz').factory('userService', [function(){
	
	var options = {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
	
    return {
    	getUsername: function() {
    		var username = $.cookie("username");
    		if (!username) {
    			username = this.createUsername();
    		}
    		//return 'cab3813a-4fe0-4355-92e5-18a2c669b817';
    		return 'a0c4886a-eb56-4e29-9202-2b4852b63e31';
    		//return username;
        },
        createUsername: function() {
    		var username = utils.createUUID();
    		$.cookie("username", username, { expires: 365, path: "/"});
    		return username;
        },
    	createSession: function() {
    		var session = createUUID();
    		$.cookie("session", session, { expires: 365, path: "/"});
    		return session;
    	},
    	getSession:	function() {
    		var session = $.cookie("session");
    		return session;
    	},
    	loginFacebook: function(facebookId) {
    		var params = {
    			'fbid' : facebookId,
    			'sessionid' : this.createSession(),
    			'url' : document.location.href
    		};
        	$http.post(Config.api + '/processUserAnswer', $.param(params), options)
    		.success(success).error(error);
    	},
    	logout: function() {
    		$.cookie("session", "0", { expires: 365, path: "/"});
    		document.location.href = document.location.href;
    	}
    };
}]);angular.module('quizz').factory('utils', function(){
	var utils = {
		shuffle: function(array) {
			var counter = array.length, temp, index;

			// While there are elements in the array
			while (counter--) {
				// Pick a random index
				index = (Math.random() * (counter + 1)) | 0;

				// And swap the last element with it
				temp = array[counter];
				array[counter] = array[index];
				array[index] = temp;
			}
			return array;
		},
		createUUID: function() {
		    // http://www.ietf.org/rfc/rfc4122.txt
		    var s = [];
		    var hexDigits = "0123456789abcdef";
		    for (var i = 0; i < 36; i++) {
		        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
		    }
		    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
		    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
		    s[8] = s[13] = s[18] = s[23] = "-";

		    var uuid = s.join("");
		    return uuid;
		},
	    isNormalNumber: function(value) {
			return ! (isNaN(value) || typeof value === undefined);
		}
	};
	return utils;
});angular.module('quizz').factory('workflowService', [function(){
	
	var questions = {};
	var userAnswers = [];
	var userFeedbacks = [];
	var currentQuestionIndex = 0;
	var numOfQuestions = 1;
	var numOfCorrectAnswers = 0;
	
	return {
		clear: function(){
			userAnswers = [];
			userFeedbacks = [];
			currentQuestionIndex = 0;
			numOfCorrectAnswers = 0;
		},
		setQuestions: function(q) {
			questions = q;
		},
		getQuestions: function() {
			return questions;
		},
		getCurrentQuestion: function() {
			return questions.gold[currentQuestionIndex];
		},
		getCurrentQuestionIndex: function() {
        	return currentQuestionIndex;
        },
		getNumOfQuestions: function() {
			return numOfQuestions;
		},
		getNumOfCorrectAnswers: function() {
			return numOfCorrectAnswers;
		},
        addUserAnswer: function(answer) {
        	userAnswers.push(answer);
        },
        getLastAnswer: function() {
        	return userAnswers[userAnswers.length-1];
        },
        addUserFeedback: function(feedback) {
        	userFeedbacks.push(feedback);
        },
        getLastFeedback: function() {
        	return userFeedbacks[userFeedbacks.length-1];
        },
        getUserFeedbacks: function() {
        	return userFeedbacks;
        },
        incCurrentQuestionIndex: function() {
        	currentQuestionIndex++;
        }
    };
	
}]);angular.module('quizz').constant('templates', {
		feedback: 'views/feedback.html?v=1',
		list: 'views/list.html?v=1',
		quiz: 'views/quiz.html?v=1',
		summary: 'views/summary.html?v=1'
});