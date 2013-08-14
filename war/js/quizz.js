	function getURLParameterByName(name) {
		name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
		var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
		return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
	}

	function getUsername() {
		var username = $.cookie("username");
		if (!username) {
			username = createUsername();
		}
		return username;
	}
	
	function createUsername() {
		var username = createUUID();
		$.cookie("username", username, { expires: 365, path: "/"});
		return username;
	}
	
	function createUUID() {
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
	}
	
	
	function getQuizzes() {
		var url = 'https://crowd-power.appspot.com/_ah/api/quiz/v1/quiz';
		var params = {
			'fields' : 'items(relation, name, questions)',
		};
		return $.getJSON(url, params);
	}
	
	function getUserQuizPerformance(user) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/quizperformance/user/' + encodeURIComponent(user);
		var params = {
			'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);	
	}
	
	function getUserQuizPerformance(quiz, user) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/quizperformance';
		url += '/quiz/' + encodeURIComponent(quiz);
		url += '/user/' + encodeURIComponent(user);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);	
	}
	
	
	function getUserTreatments(user) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/user/' + user;
		var params = {
			'fields' : 'treatments',
		};
		return $.getJSON(url, params);	
	}
	
	function displayPerformanceScores(perfomance) {
		
		$('#showScore').html("Score: "+perfomance.score+" points");

		$('#showTotalCorrect').html("Correct Answers: "+ performance.correctanswers + "/"+ performance.totalanswers);
		
		$('#showPercentageCorrect').html("Correct (%): " + performance.percentageCorrect);
		
		$('#showPercentageRank').html("Rank (%correct): "+ performance.rankPercentCorrect + "/" + performance.totalUsers +" (Top-"+(performance.rankPercentCorrect/performance.totalUsers)+")");
		
		$('#showTotalCorrectRank').html("Rank (#correct): "+ performance.rankTotalCorrect + "/" + performance.totalUsers +" (Top-"+(performance.rankTotalCorrect/performance.totalUsers)+")");
		
	}
	
	function displayFeedback(perfomance) {
		
		
	}
	
    function markConversion(type, value) {
    	// Mark a conversion in Google Analytics
		ga('send', {
			  'hitType': 'event', 
			  'hitCallback': function(){ },
			  'eventCategory': 'quiz-submission', 
			  'eventAction': type, 
			  'eventLabel': getURLParameterByName('relation'),
			  'eventValue': value, 
			  } );
    }