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
	
	function getUserQuizPerformances(user) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/quizperformance/user/' + encodeURIComponent(user);
		var params = {
			'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);	
		
		
	}
	

	function getNextQuizQuestion(quiz) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/quizquestioninstance';
		url += '/quiz/' + encodeURIComponent(quiz);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params)
			.done(function(question) { populateQuestion(question); });
		
		
	}
	
	function populateQuestion(question) {
		
		$('#relation').val(question.quiz);
		$('#mid').val(question.mid);
		$('#gold').val(question.correct);
		$('#questiontext').html(question.quizquestion);
		$('#midname').html(question.midname);
		$('#correctanswers').val(question.correctanswers);
		$('#totalanswers').val(question.totalanswers);
		
		var answers = $("#answers");
		$.each(question.answers, function(index, value) {
			answers.append($('<input id="useranswer'+index+'" name="useranswer'+index+'" type="submit" class="btn btn-primary btn-block" value="'+value+'">'));
			if (value == question.correct) {
				$('#useranswer'+index).mousedown(function(e){
					markConversion('multiple-choice-correct', 1);
				});
			} else {
				$('#useranswer'+index).mousedown(function(e){
					markConversion('multiple-choice-incorrect', 0);
				});
			}
		});
		answers.append($('<input id="idk_button" type="submit" class="btn btn-danger btn-block" name="idk" value="I don\'t know">'));
    	$("#idk_button").mousedown(function(){
    		markConversion("multiple-choice-idk", 0);
    	});
	}
	
	function getQuizQuestionInstance(quiz, mid) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/quizquestioninstance';
		url += '/quiz/' + encodeURIComponent(quiz);
		url += '/mid/' + encodeURIComponent(mid);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);	
	}
	
	function getFeedbackForPriorAnswer(user, quiz, mid) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/useranswerfeedback';
		url += '/' + encodeURIComponent(quiz);
		url += '/' + encodeURIComponent(user);
		url += '/' + encodeURIComponent(mid);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		$.getJSON(url, params)
			.done(function(feedback) { displayFeedback(feedback); });
		
	}
	
	function getUserTreatments(user) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/user/' + user;
		var params = {
			'fields' : 'treatments',
		};
		$.getJSON(url, params)
			.done(function(userdata) { applyTreatments(userdata); });
	}
	
	
	function getUserQuizPerformance(quiz, user) {
		var url = 'https://crowd-power.appspot.com/_ah/api/quizz/v1/quizperformance';
		url += '/quiz/' + encodeURIComponent(quiz);
		url += '/user/' + encodeURIComponent(user);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params)
			.done(function(performance) { displayPerformanceScores(performance); });
		
	}
	
	
	function displayPerformanceScores(performance) {
		
		$('#showScore').html("Score: "+performance.score+" points");
		$('#showTotalCorrect').html("Correct Answers: "+ performance.correctanswers + "/"+ performance.totalanswers);	
		$('#showPercentageCorrect').html("Correct (%): " + performance.percentageCorrect);
		$('#showPercentageRank').html("Rank (%correct): "+ performance.rankPercentCorrect + "/" + performance.totalUsers +" (Top-"+(performance.rankPercentCorrect/performance.totalUsers)+")");
		$('#showTotalCorrectRank').html("Rank (#correct): "+ performance.rankTotalCorrect + "/" + performance.totalUsers +" (Top-"+(performance.rankTotalCorrect/performance.totalUsers)+")");
	}
	
	function displayFeedback(feedback) {
		if (feedback.isCorrect) {
			$('#showMessage').html('The answer <span class="label label-success">'+feedback.userAnswer+'</span> was <span class="label label-success">correct</span>!');
			$('#showMessage').attr('class', 'alert alert-success');
		} else {
			$('#showMessage').html('The answer <span class="label label-important">'+feedback.userAnswer+'</span> was <span class="label label-important">incorrect</span>!');
			$('#showMessage').attr('class', 'alert alert-error');
		}
		$('#showCorrect').html('The correct answer was <span class="label label-success">'+feedback.correctAnswer+'</span>.');
		$('#showCrowdAnswers').html('Crowd performance: <span class="label label-info">'+feedback.numCorrectAnswers+' out of the '+feedback.numTotalAnswers+' users	 ('+feedback.difficulty+') answered correctly.</span>');
	}
	
	function hideDivs() {
		$('#feedback').hide();
		$('#showScore').hide();
		$('#showTotalCorrect').hide();	
		$('#showPercentageCorrect').hide();
		$('#showPercentageRank').hide();
		$('#showTotalCorrectRank').hide();
	}
	
	function applyTreatments(userdata) {
		// Show/hide the various elements, according to the user treatments.
		$.each(userdata.treatments, function(key, value) {
			var element = $(document.getElementById(key));
			if (value == true) {
				element.show();
			} else {
				element.hide();
			}
		});
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