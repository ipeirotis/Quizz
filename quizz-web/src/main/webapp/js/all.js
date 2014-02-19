angular.module('quizz', ['ngRoute', 'ngSanitize', 'ezfb'])
.config(['$routeProvider', 'templates', function($routeProvider, templates) {	
	$routeProvider
	.when('/feedback', {templateUrl: templates.feedback, controller: 'FeedbackController'})
	.when('/list', {templateUrl: templates.list, controller: 'ListController'})
	.when('/quiz', {templateUrl: templates.quiz, controller: 'QuizController', reloadOnSearch:false})
	.when('/summary', {templateUrl: templates.summary, controller: 'SummaryController'})
	.otherwise({redirectTo: '/list'});
}])

.config(['$httpProvider', '$FBProvider', function($httpProvider, $FBProvider) {
	$httpProvider.interceptors.push('interceptor');
	
	$FBProvider.setInitParams({
	    appId: '220743704753581'
	});  
}])

.run(['$rootScope', 'utils', 
    function($rootScope, utils) {
	$rootScope.utils = utils;
}]);


angular.module('quizz').controller('FeedbackController', 
		['$scope', '$routeParams', '$location', '$q', '$modal', 'questionService', 'workflowService', 'templates',
		 function ($scope, $routeParams, $location, $q, $modal, questionService, workflowService, templates) {

	$scope.feedback = workflowService.getLastFeedback();
	$scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
	$scope.numOfQuestions = workflowService.getNumOfQuestions();	
	
	$scope.challengeAnswer = function() {
		var modalPromise = $modal({template: templates.challengeAnswer, 
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
	
}]);

angular.module('quizz').controller('ListController', 
	['$scope', '$rootScope', 'quizService', 'userService', 'workflowService', '$FB',
	 function ($scope, $rootScope, quizService, userService, workflowService, $FB) {
		
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
	
	$scope.fetchUser = function(){
		userService.getUser(
			function(response) {
				workflowService.setChannelToken(response.token);
				$scope.setupChannel(response.token);
		},
			function(error) {
		});
	};
	
	$scope.fetchUser();
	
	$scope.setupChannel = function(token){
		var channel = new goog.appengine.Channel(token);
		var socket = channel.open();
		socket.onmessage = function(data){
			$rootScope.$broadcast('event:channel', data);
		};
		socket.onerror = function () {
			console.log("Error in channel gathering updates in performance");
		};
	};
	
	$scope.facebookLogin = function(){
		$FB.login(function (res) {
			if (res.authResponse) {
				updateLoginStatus(updateApiMe);
			}
		}, {scope: 'email'});
	};

	$scope.logout = function () {
		$FB.logout(function () {
			updateLoginStatus(updateApiMe);
		});
	};

	function updateLoginStatus (more) {
		$FB.getLoginStatus(function (res) {
			$scope.loginStatus = res;

			(more || angular.noop)();
		});
	}

	function updateApiMe () {
		$FB.api('/me', function (res) {
			$scope.apiMe = res;
		});
	}
	
}]);

angular.module('quizz').controller('ChallengeAnswerController', 
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
}]);

angular.module('quizz').controller('QuizController', 
	['$scope', '$rootScope', '$routeParams', '$location', 'questionService', 'quizService', 'workflowService', 'userService',
	 function ($scope, $rootScope, $routeParams, $location, questionService, quizService, workflowService, userService) {
		
		$scope.currentQuestionIndex = workflowService.getCurrentQuestionIndex() + 1;
		$scope.numOfQuestions = workflowService.getNumOfQuestions();
		
		$rootScope.$on("event:channel", function (event, data) {
			console.log(data);
			$scope.performance = data;
		});
		
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
				userID: userService.getUsername(),
				userInput: userInput || '',
				totalanswers: $scope.performance.totalanswers,
				correctanswers: $scope.performance.correctanswers,
				a: workflowService.getNumOfCorrectAnswers(),
				b: workflowService.getNumOfQuestions()-workflowService.getNumOfCorrectAnswers(),
				c: 0
			};
			questionService.sendAnswer(params,
				function(response) {
					workflowService.addUserAnswer(response.userAnswer);
					workflowService.addUserFeedback(response.userAnswerFeedback);
					workflowService.setNextQuestionGold(response.exploit);
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
	        return answer.kind == 'input-text-correct' || answer.kind == 'input_text';
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
		
}]);

angular.module('quizz').controller('SummaryController', 
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
	
	$scope.shareFacebook = function(){
		 window.open('http://www.facebook.com/sharer/sharer.php?s=100' +
         		'&p[url]=' + encodeURIComponent('http://www.quizz.us') +
         		'&p[images][0]=' +
         		'&p[title]=I+have+just+answered+' + $scope.getCorrectAnswersCount() + 
         		'+out+of+' + $scope.numOfQuestions + '+questions+correctly!',
           'facebook-share-dialog',
           'width=626,height=436');
         return false;
	};
	
	$scope.startAgain = function(){
		workflowService.clear();
		$location.path('/quiz');
	};
	
	gapi.plus.go();//TODO: check
}]);/*global angular*/

angular.module('ezfb', [])

.provider('$FB', function () {

  var NO_CALLBACK = -1;

  /**
   * Specify published apis and executable callback argument index
   *
   * ref: https://developers.facebook.com/docs/reference/javascript/
   */
  var _publishedApis = {
    // core
    api: [1, 2, 3],
    ui: 1,

    // auth
    // getAuthResponse: 0,  // deprecated
    getLoginStatus: 0,
    login: 0,
    logout: 0,

    // event
    'Event.subscribe': 1,
    'Event.unsubscribe': 1,  // not quite a callback though

    // xfbml
    'XFBML.parse': NO_CALLBACK,

    // canvas
    'Canvas.Prefetcher.addStaticResource': NO_CALLBACK,
    'Canvas.Prefetcher.setCollectionMode': NO_CALLBACK,
    'Canvas.hideFlashElement': NO_CALLBACK,
    'Canvas.scrollTo': NO_CALLBACK,
    'Canvas.setAutoGrow': NO_CALLBACK,
    'Canvas.setDoneLoading': 0,
    'Canvas.setSize': NO_CALLBACK,
    'Canvas.setUrlHandler': 0,
    'Canvas.showFlashElement': NO_CALLBACK,
    'Canvas.startTimer': NO_CALLBACK,
    'Canvas.stopTimer': 0
  };

  // Default locale
  var _locale = 'en_US';

  // Default init parameters
  var _initParams = {
    // appId      : '', // App ID from the App Dashboard
    // channelUrl : '', // Channel File for x-domain communication
    status     : true, // check the login status upon init?
    cookie     : true, // set sessions cookies to allow your server to access the session?
    xfbml      : true  // parse XFBML tags on this page?
  };
  
  // Default init function
  var _defaultInitFunction = ['$window', '$fbInitParams', function ($window, $fbInitParams) {
    // Initialize the FB JS SDK
    $window.FB.init($fbInitParams);
  }];
  var _initFunction = _defaultInitFunction;

  /**
   * Generate namespace route in an object
   *
   * @param  {object} obj   target object
   * @param  {array}  paths ordered path asc
   */
  function _pathGen(obj, paths) {
    if (paths.length === 0) {
      return;
    }
    var path = paths.shift();
    if (!obj[path]) {
      obj[path] = {};
    }
    _pathGen(obj, paths);
  }

  /**
   * Getter/setter of a config
   *
   * @param  {object} target to be configured object
   * @param  {object} config configuration(optional)
   * @return {*}             copied target if "config" is not given
   */
  function _config(target, config) {
    if (angular.isObject(config)) {
      angular.extend(target, config);
    }
    else {
      return angular.copy(target);
    }
  }

  /**
   * Context and arguments proxy function
   *
   * @param  {function} func    the function
   * @param  {object}   context the context
   * @param  {array}    args    arguments
   * @return {function}         proxied function
   */
  function _proxy(func, context, args) {
    return function () {
      return func.apply(context, args);
    };
  }

  return {
    ////////////////////////////
    // provider configuration //
    ////////////////////////////
    setInitParams: function (params) {
      _config(_initParams, params);
    },
    getInitParams: function () {
      return _config(_initParams);
    },

    setLocale: function(locale) {
      _locale = locale;
    },
    getLocale: function() {
      return _locale;
    },
    
    setInitFunction: function (func) {
      if (angular.isArray(func) || angular.isFunction(func)) {
        _initFunction = func;
      }
      else {
        throw new Error('Init function type error.');
      }
    },
    getInitFunction: function () {
      return _initFunction;
    },

    //////////
    // $get //
    //////////
    $get: [
             '$window', '$q', '$document', '$parse', '$rootScope', '$injector',
    function ($window,   $q,   $document,   $parse,   $rootScope,   $injector) {
      var _initReady, _$FB, 
          _savedListeners = {};

      var _paramsReady = $q.defer();

      if (_initParams.appId || _initFunction !== _defaultInitFunction) {
        _paramsReady.resolve();
      }

      /**
       * #fb-root check & create
       */
      if (!$document[0].getElementById('fb-root')) {
        $document.find('body').append('<div id="fb-root"></div>');
      }

      _initReady = $q.defer();
      // Load the SDK's source Asynchronously
      (function(d){
        var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
        if (d.getElementById(id)) {return;}
        js = d.createElement('script'); js.id = id; js.async = true;
        js.src = "//connect.facebook.net/" + _locale + "/all.js";
        ref.parentNode.insertBefore(js, ref);
      }($document[0]));

      $window.fbAsyncInit = function () {
        _paramsReady.promise.then(function() {
          // Run init function
          $injector.invoke(_initFunction, null, {'$fbInitParams': _initParams});

          _$FB.$$ready = true;
          _initReady.resolve();
        });
      };

      _$FB = {
        $$ready: false,
        init: function (params) {
          _config(_initParams, params);
          _paramsReady.resolve();
        }
      };

      /**
       * _$FB initialization
       *
       * Publish FB APIs with auto-check ready state
       */
      angular.forEach(_publishedApis, function (cbArgIndex, apiPath) {
        _pathGen(_$FB, apiPath.split(/\./));

        var getter = $parse(apiPath),
            setter = getter.assign;
        setter(_$FB, function () {

          var args = Array.prototype.slice.call(arguments),
              apiCall = _proxy(function (args) {
                var dfd = $q.defer(),
                    /**
                     * Add or replce original callback function with deferred resolve
                     * 
                     * @param  {number} index expected api callback index
                     */
                    replaceCallbackAt = function (index) {
                      var func = angular.isFunction(args[index]) ? args[index] : angular.noop,
                          newFunc = function () {
                            var funcArgs = Array.prototype.slice.call(arguments);

                            if ($rootScope.$$phase) {
                              // already in angularjs context
                              func.apply(null, funcArgs);
                              dfd.resolve.apply(dfd, funcArgs);
                            }
                            else {
                              // not in angularjs context
                              $rootScope.$apply(function () {
                                func.apply(null, funcArgs);
                                dfd.resolve.apply(dfd, funcArgs);
                              });
                            }
                          };

                      while (args.length <= index) {
                        args.push(null);
                      }

                      /**
                       * `FB.Event.unsubscribe` requires the original listener function.
                       * Save the mapping of original->wrapped on `FB.Event.subscribe` for unsubscribing.
                       */
                      var eventName;
                      if (apiPath === 'Event.subscribe') {
                        eventName = args[0];
                        if (angular.isUndefined(_savedListeners[eventName])) {
                          _savedListeners[eventName] = [];
                        }
                        _savedListeners[eventName].push({
                          original: func,
                          wrapped: newFunc
                        });
                      }
                      else if (apiPath === 'Event.unsubscribe') {
                        eventName = args[0];
                        if (angular.isArray(_savedListeners[eventName])) {
                          var i, subscribed, l = _savedListeners[eventName].length;
                          for (i = 0; i < l; i++) {
                            subscribed = _savedListeners[eventName][i];
                            if (subscribed.original === func) {
                              newFunc = subscribed.wrapped;
                              _savedListeners[eventName].splice(i, 1);
                              break;
                            }
                          }
                        }
                      }

                      // Replace the original one (or null) with newFunc
                      args[index] = newFunc;
                    };

                if (cbArgIndex !== NO_CALLBACK) {
                  if (angular.isNumber(cbArgIndex)) {
                    /**
                     * Constant callback argument index
                     */
                    replaceCallbackAt(cbArgIndex);
                  }
                  else if (angular.isArray(cbArgIndex)) {
                    /**
                     * Multiple possible callback argument index
                     */
                    var i, c;
                    for (i = 0; i < cbArgIndex.length; i++) {
                      c = cbArgIndex[i];

                      if (args.length == c ||
                          args.length == (c + 1) && angular.isFunction(args[c])) {

                        replaceCallbackAt(c);

                        break;
                      }
                    }
                  }
                }

                /**
                 * Apply back to original FB SDK
                 */
                var origFBFunc = getter($window.FB);
                if (!origFBFunc) {
                  throw new Error("Facebook API `FB." + apiPath + "` doesn't exist.");
                }
                origFBFunc.apply($window.FB, args);

                return dfd.promise;
              }, null, [args]);

          /**
           * Wrap the api function with our ready promise
           */
          if (cbArgIndex === NO_CALLBACK) {
            // Do not return promise for no-callback apis
            _initReady.promise.then(apiCall); 
          }
          else {
            return _initReady.promise.then(apiCall);
          }
        });
      });

      return _$FB;
    }]
  };
})

/**
 * @ngdoc directive
 * @name ng.directive:ezfbXfbml
 * @restrict EAC
 *
 * @description
 * Parse XFBML inside the directive
 *
 * @param {boolean} ezfb-xfbml Reload trigger for inside XFBML,
 *                             should keep only XFBML content inside the directive.
 * @param {expr}    onrender   Evaluated every time content xfbml gets rendered.
 */
.directive('ezfbXfbml', [
         '$FB', '$parse', '$compile', '$timeout',
function ($FB,   $parse,   $compile,   $timeout) {
  return {
    restrict: 'EAC',
    compile: function (tElm, tAttrs) {
      var _savedHtml = tElm.html();

      return function postLink(scope, iElm, iAttrs) {
        var _rendering = true,
            onrenderExp = iAttrs.onrender || '',
            onrenderHandler = function () {
              if (_rendering) {
                scope.$eval(onrenderExp);
                _rendering = false;
              }
            },
            renderEvent = 'xfbml.render';

        /**
         * Render event
         */
        if (onrenderExp) {
          // Subscribe
          $FB.Event.subscribe(renderEvent, onrenderHandler);

          // Unsubscibe on $destroy
          iElm.bind('$destroy', function () {
            $FB.Event.unsubscribe(renderEvent, onrenderHandler);

            iElm.unbind('$destroy');
          });
        }

        $FB.XFBML.parse(iElm[0]);

        /**
         * The trigger
         */
        var setter = $parse(iAttrs.ezfbXfbml).assign;
        scope.$watch(iAttrs.ezfbXfbml, function (val) {
          if (val) {
            _rendering = true;
            iElm.html(_savedHtml);

            $compile(iElm.contents())(scope);
            $timeout(function () {
              $FB.XFBML.parse(iElm[0]);
            });

            // Reset the trigger if it's settable
            (setter || angular.noop)(scope, false);
          }
        }, true);

      };
    }
  };
}]);
angular.module('quizz').factory('interceptor', 
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
	    			  'eventValue': Math.round(100.*response.score/response.totalanswers),
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
}]);angular.module('quizz').factory('userService', ['$http', '$rootScope', function($http, $rootScope){
	
	var options = {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}};
	
    return {
    	getUser: function(success, error) {
    		var url = Config.api + '/user?userid=' + this.getUsername();
        	$http.get(url).success(success).error(error);
        },
    	getUsername: function() {
    		var username = $.cookie("username");
    		if (!username) {
    			username = this.createUsername();
    		}
    		return username;
        },
        createUsername: function() {
    		var username = $rootScope.utils.createUUID();
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
		},
		toPercentage: function(fValue) {
			return (100. * fValue).toFixed(0) + "%";
		},
		safeNumber: function(value) {
			return  this.isNormalNumber(value) ? value.toString() : "---" ;
		},
		toPercentage: function(fValue) {
			return (100. * fValue).toFixed(0) + "%";
		},
		toSafePercentage: function(fValue) {
			if (this.isNormalNumber(fValue))
				return this.toPercentage(fValue);
			else
				return "---";
		}
	};
	return utils;
});angular.module('quizz').factory('workflowService', [function(){
	
	var questions = {};
	var userAnswers = [];
	var userFeedbacks = [];
	var currentQuestionIndex = 0;
	var currentGoldQuestionIndex = 0;
	var currentSilverQuestionIndex = 0;
	var numOfQuestions = 10;
	var numOfGoldQuestions = 0;
	var numOfSilverQuestions = 0;
	var numOfCorrectAnswers = 0;
	var isNextQuestionGold = true;
	var channelToken = '';
	
	return {
		clear: function(){
			userAnswers = [];
			userFeedbacks = [];
			currentQuestionIndex = 0;
			numOfCorrectAnswers = 0;
			numOfGoldQuestions = 0;
			numOfSilverQuestions = 0;
		},
		setQuestions: function(q) {
			questions = q;
			if(q.gold){
				numOfGoldQuestions = q.gold.length;
			}
			if(q.silver){
				numOfSilverQuestions = q.silver.length;
			}
		},
		getQuestions: function() {
			return questions;
		},
		getCurrentQuestion: function() {
			if((isNextQuestionGold == true && currentQuestionIndex < numOfGoldQuestions) ||
					currentQuestionIndex >= numOfSilverQuestions){
				return questions.gold[currentQuestionIndex];
			}else{
				return questions.silver[currentQuestionIndex];
			}
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
        	currentGoldQuestionIndex++;
        	currentSilverQuestionIndex++;
        },
        setChannelToken: function(t) {
        	channelToken = t;
		},
		getChannelToken: function(){
			return channelToken;
		},
		setNextQuestionGold: function(g){
			isNextQuestionGold = g;
		}
    };
	
}]);angular.module('quizz').constant('templates', {
		challengeAnswer: 'views/modals/challengeAnswer.html',
		feedback: 'views/feedback.html?v=1',
		list: 'views/list.html?v=1',
		quiz: 'views/quiz.html?v=1',
		summary: 'views/summary.html?v=1'
});