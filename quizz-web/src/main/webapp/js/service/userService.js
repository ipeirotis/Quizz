angular.module('quizz').factory('userService', ['$http', '$rootScope', function($http, $rootScope){
	
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
}]);