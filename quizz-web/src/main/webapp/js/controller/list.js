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