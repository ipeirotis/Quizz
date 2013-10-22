
NUM_QUESTIONS = 10;
CURRENT_QUIZZ = -1;
QUIZZ_QUESTIONS = new Array();
USER_ANSWERS = new Array();

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
			'fields' : 'items(quizID, name, questions)',
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
		$('#form').html($('#quizEndSummary').html());
		var correctCount = 0;
		for (var i=0;i<USER_ANSWERS.length;i++) {
			if (clearString(QUIZZ_QUESTIONS[i].correct) === clearString(USER_ANSWERS[i])) {
				correctCount++;
			}
		}
		$('#correctCountSummary').html(correctCount);
		$('#totalCountSummary').html(QUIZZ_QUESTIONS.length);
		updateSharingParameters(correctCount, QUIZZ_QUESTIONS.length);
	}

	function updateSharingParameters(correctCount, totalCount) {
		var sbb = $("#sharingButtonsBox");
		sbb.html(sbb.html().replace("CORRECT_COUNT", correctCount.toString())
			.replace("TOTAL_COUNT", totalCount.toString()));
		gapi.plus.go();
	}

	function answeredQuestion (answerID){
		return function () {
			var formData = $('#addUserEntry').serializeArray();
			formData.push({'name': 'answerID', 'value': answerID});
			hideScoresMakeLoading();
			hideFeedback();
			makeLoadingScreen("Loading feedback");
			sendSingleQuestionResults(formData).done(
				function (feedback) {
					disableLoadingScreen();
					hideQuestion();
					showFeedback(feedback, prepareNextQuestion);
			});
			USER_ANSWERS.push(answerID);
			return false;
		}
	}

	function clearString(str) {
		return $.trim(str).replace(/"+$/, "").replace(/^"+/, "");
	}

	function populateQuestion(question) {

		$('#quizID').val(question.quizID);
		$('#questionID').val(question.id);
		$('#gold').val(question.correct);
		$('#questiontext').html(question.text);
		$('#correctanswers').val(question.correctanswers);
		$('#totalanswers').val(question.totalanswers);

		var answers = $("#answers");
		shuffle(question.answers);
		$.each(question.answers, function(index, answer) {
			value = answer.text
			//  triming " chars and escaping internal ones
			value = clearString(value);
			value = $.trim(value).replace(/"/, "\\\"");
			var uaid = "useranswer" + index;
			var huaid = '#' + uaid;
			answers.append($('<input id="'+uaid+'" name="'+uaid+'" type="submit" class="btn btn-primary btn-block" value="'+value+'">'));
			var gatype = 'multiple-choice-' + (answer.isGold ? "" : "in") + 'correct';
			var ganumber = answer.isGold ? 1 : 0 ;
			$(huaid).mousedown(function(e){markConversion(gatype, ganumber);});
			$(huaid).click(answeredQuestion (answer.id));
		});
		answers.append($('<input id="idk_button" type="submit" class="btn btn-danger btn-block" name="idk" value="I don\'t know">'));
    	$("#idk_button").mousedown(function(){
    		markConversion("multiple-choice-idk", 0);
    	});
    	$('#idk_button').click(answeredQuestion (-1));
	}

	function getFeedbackForPriorAnswer(user, quiz, questionID) {
		var url = getAPIURL() + 'useranswerfeedback';
		url += '/' + encodeURIComponent(quiz);
		url += '/' + encodeURIComponent(user);
		url += '/' + encodeURIComponent(questionID);
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

	function isNormalNumber(value) {
		return ! (isNaN(value) || typeof value === "undefined");
	}

	function safeNumber(value) {
		return  isNormalNumber(value) ? value.toString() : "---" ;
	}

	function toSafePercentage(fValue) {
		if (isNormalNumber(fValue))
			return toPercentage(fValue);
		else
			return "---";
	}

	function ranksFormating(kind, userValue, totalValue) {
		var prefix = "Rank (" + kind + "correct): ";
		var sufix = "---";
		if (isNormalNumber(userValue) && isNormalNumber(totalValue)) {
			var position = userValue / totalValue;
			sufix = "" + userValue + "/" + totalValue + " (Top " + toPercentage(position) + ")";
		}
		return prefix + sufix;
	}


	function displayPerformanceScores(performance) {
		$('#showScore').html("Score: " + performance.score.toFixed(3) + " points");
		$('#showTotalCorrect').html("Correct Answers: "+ performance.correctanswers + "/"+ performance.totalanswers);
		$('#showPercentageCorrect').html("Correct (%): " + toSafePercentage(performance.percentageCorrect));
		$('#showPercentageRank').html(ranksFormating("%", performance.rankPercentCorrect, performance.totalUsers));
		$('#showTotalCorrectRank').html(ranksFormating("#", performance.rankTotalCorrect, performance.totalUsers));

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

		var crowdPerformance;
		if (isNormalNumber(feedback.numCorrectAnswers) && isNormalNumber(feedback.numTotalAnswers)) {
			crowdPerformance = feedback.numCorrectAnswers+' out of the '+feedback.numTotalAnswers+' users	 ('+feedback.difficulty+') answered correctly.';
		} else {
			crowdPerformance = "not available yet ...";
		}
		$('#showCrowdAnswers').html('Crowd performance: <span class="label label-info">' + crowdPerformance + '</span>');
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
			  'eventLabel': getURLParameterByName('quizID'),
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

function setFeedbackBtnMsg(val) {
	$('#skipFeedbackBtn').attr('value', "Next question ... (" + val + ")");
}

function showFeedback(feedback, callbackf) {
	displayFeedback(feedback);
    $('#feedback').append('<input id="skipFeedbackBtn" type="submit"' +
        ' class="btn btn-info"/>' );
    setFeedbackBtnMsg(5);
    $('#feedback').show();
    var executedCallback = false;

    var intervalId = setInterval(function () {
    	var valAttr = $('#skipFeedbackBtn').attr('value');
    	var oldVal = parseInt(valAttr.substr(valAttr.indexOf("(") + 1, 1));
    	setFeedbackBtnMsg(oldVal - 1);
    	if (oldVal === 1) {
    		clearInterval(intervalId);
		    $('#feedback').fadeOut(600, function () {
		    	executedCallback = true;
		    	callbackf();
		    });
    	}
    }, 1000);

    $('#skipFeedbackBtn').click (function () {
    	clearInterval(intervalId);
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
