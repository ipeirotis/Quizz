
NUM_QUESTIONS = 10;
CURRENT_QUIZZ = -1;
QUIZZ_QUESTIONS = new Array();

function fst(array) {
	return array[0];
}

// From SO: http://stackoverflow.com/a/6274398/1585082
function shuffle(array) {
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
}

	function getBaseURL() {
		return 'https://crowd-power.appspot.com/'
	}

	function getWebURL() {
		return 'http://www.quizz.us/';
	}

	function getAPIURL() {
		return getBaseURL() + '_ah/api/quizz/v1/';
	}

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
		var url = getAPIURL() + 'quiz';
		var params = {
			'fields' : 'items(relation, name, questions)',
		};
		return $.getJSON(url, params);
	}

	function getUserQuizPerformances(user) {
		var url = getAPIURL() + 'quizperformance/user/' + encodeURIComponent(user);
		var params = {
			'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);
	}

	function getNextQuizQuestions(quiz, numQuestions) {
		CURRENT_QUIZZ = -1;
		numQuestions = numQuestions || NUM_QUESTIONS;
		var url = getAPIURL() + 'quizquestions/';
		url += encodeURIComponent(quiz);
		var params = {
			'num' : numQuestions,
		};
		return $.getJSON(url, params);
	}

	function initializeQuizzWithQuestions(questions) {
		QUIZZ_QUESTIONS = questions.items;
		presentNextQuestion();
	}

	function presentNextQuestion() {
		CURRENT_QUIZZ += 1;
		$('#answers').html("");
		$('#questionsPackProgress').html("Question " + (CURRENT_QUIZZ + 1) +
			" out of " + QUIZZ_QUESTIONS.length);
		populateQuestion(QUIZZ_QUESTIONS[CURRENT_QUIZZ]);
		showQuestion();
	}

	function prepareNextQuestion() {
		if (CURRENT_QUIZZ === QUIZZ_QUESTIONS.length - 1) {
			endOfQuizzPack();
		} else {
			presentNextQuestion();
		}
	}

	function sendSingleQuestionResults(formData) {
		var url = getWebURL() + 'processUserAnswer';
		return $.post(url, formData)
			.fail( function(jqXHR, textStatus, errorThrown) {
				console.log("Sending your answer failed: " + textStatus);
			});
	}

	function endOfQuizzPack () {
		$('#addUserEntry').hide();
		$('#questionsPackProgress').hide();
		$('#form').html("Thank you for completing quizz. " +
		 "Refresh page to start egain. Your statistics are now being updated ...");
	}

	function answeredQuestion (nname, vvalue){
		return function () {
			var formData = $('#addUserEntry').serializeArray();
			formData.push({name: nname, value: vvalue});
			var quiz = QUIZZ_QUESTIONS[CURRENT_QUIZZ].mid;
			hideScoresMakeLoading();
			hideFeedback();
			makeLoadingScreen("Loading feedback");
			sendSingleQuestionResults(formData).done(
				function (feedback) {
					disableLoadingScreen();
					hideQuestion();
					showFeedback(feedback, prepareNextQuestion);
			});
			return false;
		}
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
		shuffle(question.answers);
		$.each(question.answers, function(index, value) {
			//  triming " chars and escaping internal ones
			value = $.trim(value).replace(/"+$/, "").replace(/^"+/, "");
			value = $.trim(value).replace(/"/, "\\\"");
			var uaid = "useranswer" + index;
			var huaid = '#' + uaid;
			answers.append($('<input id="'+uaid+'" name="'+uaid+'" type="submit" class="btn btn-primary btn-block" value="'+value+'">'));
			var gatype = 'multiple-choice-' + (value == question.correct ? "" : "in") + 'correct';
			var ganumber = value == question.correct ? 1 : 0 ;
			$(huaid).mousedown(function(e){markConversion(gatype, ganumber);});
			$(huaid).click(answeredQuestion (uaid, value));
		});
		answers.append($('<input id="idk_button" type="submit" class="btn btn-danger btn-block" name="idk" value="I don\'t know">'));
    	$("#idk_button").mousedown(function(){
    		markConversion("multiple-choice-idk", 0);
    	});
    	$('#idk_button').click(answeredQuestion ("idk", "I don\'t know"));
	}

	function getQuizQuestionInstance(quiz, mid) {
		var url = getAPIURL() + 'quizquestioninstance';
		url += '/quiz/' + encodeURIComponent(quiz);
		url += '/mid/' + encodeURIComponent(mid);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);
	}

	function getFeedbackForPriorAnswer(user, quiz, mid) {
		var url = getAPIURL() + 'useranswerfeedback';
		url += '/' + encodeURIComponent(quiz);
		url += '/' + encodeURIComponent(user);
		url += '/' + encodeURIComponent(mid);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);
	}

	function getUserTreatments(user) {
		var url = getAPIURL() + 'user/' + user;
		var params = {
			'fields' : 'treatments',
		};
		return $.getJSON(url, params);
	}


	function getUserQuizPerformance(quiz, user) {
		var url = getAPIURL() + 'quizperformance';
		url += '/quiz/' + encodeURIComponent(quiz);
		url += '/user/' + encodeURIComponent(user);
		var params = {
			//'fields' : 'items(quiz, totalanswers)',
		};
		return $.getJSON(url, params);
	}

	function toPercentage(fValue) {
		return (100. * fValue).toFixed(0) + "%"
	}


	function displayPerformanceScores(performance) {
		$('#showScore').html("Score: " + performance.score.toFixed(3) + " points");
		$('#showTotalCorrect').html("Correct Answers: "+ performance.correctanswers + "/"+ performance.totalanswers);
		$('#showPercentageCorrect').html("Correct (%): " + toPercentage(performance.percentageCorrect));
		$('#showPercentageRank').html("Rank (%correct): "+ performance.rankPercentCorrect + "/" + performance.totalUsers +" (Top-"+toPercentage(performance.rankPercentCorrect/performance.totalUsers)+")");
		$('#showTotalCorrectRank').html("Rank (#correct): "+ performance.rankTotalCorrect + "/" + performance.totalUsers +" (Top-"+toPercentage(performance.rankTotalCorrect/performance.totalUsers)+")");

		$("#inScores").show();
		$("#scoresLoading").hide();
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
		$('#showMessage').hide();
		$('#showCorrect').hide();
		$('#showCrowdAnswers')
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


function makeLoadingScreen(msg) {
	$('#scores').hide();
	$("#addUserEntry").hide();
	$("#questionsPackProgress").hide();
	$("#loadingMsg").html(msg);
	$("#loadingScreen").show();
}

function disableLoadingScreen() {
	$("#loadingScreen").hide();
	$('#scores').show();
	$("#questionsPackProgress").show();
	$("#addUserEntry").show();
}

function hideFeedback() {
    var feedbackdiv = $('#feedback');
    feedbackdiv.empty();
    feedbackdiv.hide();
    feedbackdiv.append($('<div id="showMessage"></div>'));
    feedbackdiv.append($('<div class="alert alert-success" id="showCorrect"></div>'));
    feedbackdiv.append($('<div class="alert alert-info" id="showCrowdAnswers"></div>'));
}

function showFeedback(feedback, callbackf) {
	displayFeedback(feedback);
    $('#feedback').append('<input id="skipFeedbackBtn" type="submit"' +
    	' class="btn btn-info" value="Skip feedback ..." />' );
    $('#feedback').show();
    var executedCallback = false;
    $('#feedback').delay(5000).fadeOut(600, function () {
    	executedCallback = true;
    	callbackf();
    });
    $('#skipFeedbackBtn').click (function () {
    	$('#feedback').stop(true, true);
    	$('#feedback').hide();
    	if (!executedCallback) callbackf();
    });
}

function setUpPerformanceUpdatesChannel(token) {
	var channel = new goog.appengine.Channel(token);
	var socket = channel.open();
	socket.onmessage = handlePerfChannelMessage;
	socket.onerror = function () {
		console.log("Error in channel gathering updates in performance");
	}
}

function hideQuestion() {
	$("#addUserEntry").hide();
}

function showQuestion() {
	$("#addUserEntry").show();
}

function hideScoresMakeLoading() {
	$("#inScores").hide();
	$("#scoresLoading").show();
}

function handlePerfChannelMessage (msg) {
	var perfData = $.parseJSON(msg.data);
	displayPerformanceScores(perfData);
}
