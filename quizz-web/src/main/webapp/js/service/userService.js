angular.module('quizz').factory('userService', [function(){
	
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
}]);