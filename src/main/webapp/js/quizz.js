
NUM_QUESTIONS = 10;
CURRENT_QUIZZ = -1;
QUIZZ_QUESTIONS = [];
USER_ANSWERS = [];
USER_FEEDBACKS = [];

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

	function getWebURL() {
		return window.location.origin + "/";
	}

	function getAPIURL() {
		return 'https://crowd-power.appspot.com/_ah/api/quizz/v1/';
	}
	
	function getFBAppID() {
		return '220743704753581';
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
		//return "3a94d8ff-dfc6-4c1e-94d2-3f6548f80aaa";
	}

	function createUsername() {
		var username = createUUID();
		$.cookie("username", username, { expires: 365, path: "/"});
		return username;
	}

	function createSession() {
		var session = createUUID();
		$.cookie("session", session, { expires: 365, path: "/"});
		return session;
	}

	function getSession() {
		var session = $.cookie("session");
		return session;
	}

	function loginFB(fbid) {
		var url = getWebURL() + 'fblogin';
		sessionid = createSession();
		var params = {
			'fbid' : fbid,
			'sessionid' : sessionid,
			'url' : document.location.href
		};
		return $.post(url, params);
	}
	function getUser() {
		userid = getUsername();
		var url = getAPIURL() + "user/" + userid;
		return $.getJSON(url);
	}

	function logout() {
		$.cookie("session", "0", { expires: 365, path: "/"});
		document.location.href = document.location.href;
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
		/*$('#questionsPackProgress').html("Question " + (CURRENT_QUIZZ + 1) +
			" out of " + QUIZZ_QUESTIONS.length);*/
		
		// fix question skipping
		var currentQuizzNo = CURRENT_QUIZZ + 1;
		$('#questionsPackProgress').html("Question " + (currentQuizzNo) +
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
		for (var i=0;i<USER_FEEDBACKS.length;i++) {
			if (USER_FEEDBACKS[i].isCorrect) {
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

	function answeredQuestion (answer){
		return function () {
			var formData = $('#addUserEntry').serializeArray();
			var answerID = (answer === null) ? -1 : answer.internalID;
			var userInput = $('#userInputField').val();
			formData.push({'name': 'answerID', 'value': answerID});
			formData.push({'name': 'userInput', 'value': userInput});
			hideScoresMakeLoading();
			hideFeedback();
			makeLoadingScreen("Loading feedback");
			sendSingleQuestionResults(formData).done(
				function (feedback) {
					USER_FEEDBACKS.push(feedback);
					disableLoadingScreen();
					hideQuestion();
					showFeedback(feedback, prepareNextQuestion);
			});
			USER_ANSWERS.push(answer);
			return false;
		}
	}

	function clearString(str) {
		return $.trim(str).replace(/"+$/, "").replace(/^"+/, "");
	}

	function generateSelectableAnswer(htmlAnswers, index, answer, gatype, ganumber) {
		value = answer.text
		//  triming " chars and escaping internal ones
		value = clearString(value);
		value = $.trim(value).replace(/"/, "\\\"");
		var uaid = "useranswer" + index;
		var huaid = '#' + uaid;
		htmlAnswers.append($('<input id="'+uaid+'" name="'+uaid+'" type="submit" class="btn btn-primary btn-block" value="'+value+'">'));
		$(huaid).mousedown(function(e){markConversion(gatype, ganumber);});
		$(huaid).click(answeredQuestion (answer));
	}

	function generateSelectableWrongAnswer(htmlAnswers, index, answer) {
		var ganumber = 0;
		var gatype = 'multiple-choice-incorrect';
		generateSelectableAnswer(htmlAnswers, index, answer, gatype, ganumber);
	}

	function generateSelectableGoldAnswer(htmlAnswers, index, answer) {
		var ganumber = 1;
		var gatype = 'multiple-choice-correct';
		generateSelectableAnswer(htmlAnswers, index, answer, gatype, ganumber);
	}

	function generateInputTextAnswer(htmlAnswers, index, answer) {
		var ganumber = 1;
		var gatype = 'input-text-correct';
		var uaid = "useranswer" + index;
		var huaid = '#' + uaid;
		
		htmlAnswers.append('<span style="vertical-align: text-bottom;">Your answer: </span> <input id="userInputField" type="text" name="userInput">');
		htmlAnswers.append($('<input id="'+uaid+'" name="'+uaid+'" type="submit" class="btn btn-primary btn-block" value="Send">'));
		$(huaid).mousedown(function(e){markConversion(gatype, ganumber);});
		$(huaid).click(answeredQuestion (answer));
	}

ANSWERS_GENERATORS = {
	"selectable_gold": generateSelectableGoldAnswer,
	"selectable_not_gold": generateSelectableWrongAnswer,
	"input_text": generateInputTextAnswer,
}

	function generateAnswers(answers) {
		var htmlAnswers = $("#answers");
		shuffle(answers);
		$.each(answers, function(index, answer) {
			ANSWERS_GENERATORS[answer.kind](htmlAnswers, index, answer);
		});
		htmlAnswers.append($('<input id="idk_button" type="submit" class="btn btn-danger btn-block" name="idk" value="I don\'t know">'));
    	$("#idk_button").mousedown(function(){
    		markConversion("multiple-choice-idk", 0);
    	});
    	$('#idk_button').click(answeredQuestion (null));
	}

	function populateQuestion(question) {

		$('#quizID').val(question.quizID);
		$('#questionID').val(question.id);
		$('#questiontext').html(question.text);

		generateAnswers(question.answers);
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
		$('#showRankScore').html(ranksFormating("%", performance.rankScore, performance.totalUsers));
		$('#correctanswers').val(performance.correctanswers);
		$('#totalanswers').val(performance.totalanswers);

		$("#inScores").show();
		$("#scoresLoading").hide();
	}

	function displayFeedback(feedback) {
		var newBadgeHtml = 'New Badges: ';
		if(feedback.userNewBadges != "" && $('#showNewBadges').hasClass('show')){
			$('#showNewBadges').html(newBadgeHtml + feedback.userNewBadges);
			$('#showNewBadges').show();
		} else {
			$('#showNewBadges').hide();
		}
		if (feedback.isCorrect) {
			$('#showMessage').html('The answer <span class="label label-success">'+feedback.userAnswerText+'</span> was <span class="label label-success">correct</span>!');
			$('#showMessage').attr('class', 'alert alert-success');
			//$('#showCorrect').hide();
			$('#showCorrect').show().html('The answer <span class="label label-success">'+feedback.userAnswerText+'</span>.');
		} else {
			if (feedback.userAnswerText) {
				$('#showMessage').html('The answer <span class="label label-important">'+feedback.userAnswerText+'</span> was <span class="label label-important">incorrect</span>!');
				$('#showMessage').attr('class', 'alert alert-error');
			}
			$('#showCorrect').show().html('The correct answer was <span class="label label-success">'+feedback.correctAnswerText+'</span>.');
		}

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
		$('#showNewBadges').hide();
		$('#showMessage').hide();
		$('#showCorrect').hide();
		$('#showCrowdAnswers')
		$('#showScore').hide();
		$('#showTotalCorrect').hide();
		$('#showPercentageCorrect').hide();
		$('#showRankScore').hide();
	}

	function applyTreatments(userdata) {
		// Show/hide the various elements, according to the user treatments.
		$.each(userdata.treatments, function(key, value) {
			var element = $(document.getElementById(key));
			if (value == true) {
				element.show();
				element.addClass('show');
			} else {
				element.hide();
			}
		});
	}

    function markConversion(type, value) {
		var url = getAPIURL() + 'quizperformance';
		url += '/quiz/' + encodeURIComponent(getURLParameterByName('quizID'));
		url += '/user/' + encodeURIComponent(getUsername());
    	$.ajax( {
    		type:'Get',
    		url:url,
    		success:function(data) {	
   			JSON.stringify(data);
        	// Mark a conversion in Google Analytics
    		ga('send', {
    			  'hitType': 'event',
    			  'hitCallback': function(){ },
    			  'eventCategory': 'quiz-submission',
    			  'eventAction': type,
    			  'eventLabel': getURLParameterByName('quizID'),
    			  'eventValue': 1.0*data.score/data.totalanswers,
    			  } );
    		}
    		});
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
    feedbackdiv.hide();
}

function setupDivs() {
    var feedbackdiv = $('#feedback');
    feedbackdiv.empty();
    feedbackdiv.append($('<div class="alert alert-warning" id="showNewBadges"></div>'));
    feedbackdiv.append($('<div id="showMessage"></div>'));
    feedbackdiv.append($('<div class="alert alert-success" id="showCorrect"></div>'));
    feedbackdiv.append($('<div class="alert alert-info" id="showCrowdAnswers"></div>'));
}

function setFeedbackBtnMsg(val) {
  $('#skipFeedbackBtn').attr('value', "Next question ... (" + val + ")");
}

// Shows the feedback page of the previous question and calls the callbackf
// if either the user clicks on the #skipFeedbackBtn or 5 seconds elapsed
// if there is another question.
function showFeedback(feedback, callbackf) {
  displayFeedback(feedback);
  if($('#feedback').has("#skipFeedbackBtn").length == 0){
    $('#feedback').append('<input id="skipFeedbackBtn" type="submit"' +
        ' class="btn btn-info"/>');
  }
  $('#feedback').show();

  var executedCallback = false;
  var intervalId;
  if (CURRENT_QUIZZ == QUIZZ_QUESTIONS.length - 1) {
    $('#skipFeedbackBtn').val("Show Results")
  } else {
    // Set up event to go to next question after 5 seconds.
    setFeedbackBtnMsg(5);
    intervalId = setInterval(function () {
      var valAttr = $('#skipFeedbackBtn').attr('value');
      var oldVal = parseInt(valAttr.substr(valAttr.indexOf("(") + 1, 1));
      setFeedbackBtnMsg(oldVal - 1);
      if (oldVal === 1) {
        // Reset event handler for going to next question.
        clearInterval(intervalId);
        $('#skipFeedbackBtn').unbind('click');
        $('#feedback').fadeOut(600, function () {
          if (!executedCallback) {
            executedCallback = true;
            callbackf();
          }
        });
      }
    }, 1000);
  }

  // Set up click handler to go to next question when clicked.
  $('#skipFeedbackBtn').click (function () {
    // Reset event handler for going to next question.
    clearInterval(intervalId);
    $('#skipFeedbackBtn').unbind('click');
    $('#feedback').stop(true, true);
    $('#feedback').hide();
    if (!executedCallback) {
      executedCallback = true;
      callbackf();
    }
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
