<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<jsp:include page="/header.jsp"><jsp:param name="title" value="Test yourself, Compare yourself, Learn new things" /></jsp:include>
<body>
<div id="fb-root"></div>
<script>
	window.fbAsyncInit = function() {
		FB.init({
			appId      : getFBAppID(), // App ID
			channelUrl : '//localhost:8888/channel.html', // Channel File
			status     : true, // check login status
			cookie     : true, // enable cookies to allow the server to access the session
			xfbml      : true  // parse XFBML
		});
		FB.Event.subscribe('auth.authResponseChange', function(response) {
			if (response.status === 'connected') {
			} else if (response.status === 'not_authorized') {
				FB.login(function(response) { 
					$('#login').hide();
					$('#logout').show();
				});
			} else {
				FB.login(function(response) { 
					$('#login').hide();
					$('#logout').show();
				});
			}
		});
	};
	// Load the SDK asynchronously
	(function(d){
		var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
	   if (d.getElementById(id)) {return;}
	   js = d.createElement('script'); js.id = id; js.async = true;
	   js.src = "//connect.facebook.net/en_US/all.js";
	   ref.parentNode.insertBefore(js, ref);
	}(document));
	</script>
	
	<div class="container" style="text-align: center; max-width: 640px">
        <jsp:include page="/logo.jsp"></jsp:include>
		<span id='login' style='display: none'>
			<a id='facebook-login' href="#"><img src="./assets/facebook_button.png" width="160px" height="80px"></a>
		</span>
		<span id='logout' style='display: none'><a href="#">Logout</a></span>
		<table class="table table-striped  table-bordered" id="quizzes">
			<tbody>
				<tr>
					<th>Quiz</th>
				</tr>
			</tbody>

		</table>
	</div>

	<script type="text/javascript">

	$(document).ready(function() {
		var loggedin = false;
		
		function showLogin(){
			$('#logout').hide();
			$('#login').show();
		}
		function hideLogin(){
			$('#login').hide();
			$('#logout').show();
		}
		
		$.when(getUser()).done(function(response){
			try{
				if(response.responseJSON.sessionid == getSession()) {
					loggedin = true;
					hideLogin();
				} else {
					showLogin();
				}
			} catch(err) {
				showLogin();
			}
		}).fail(function(){
			showLogin();
		});
		$('#facebook-login').on('click', function(){
			facebookID = FB.getUserID();
			if(facebookID != '') {
				$.when(loginFB(facebookID)).done(function(){
					hideLogin();
				}).fail(function(){
					showLogin();
				});
			} else {
	      		FB.login(function(response) { 
			        if ( response.status === 'connected' ) {
						FBID = response.authResponse.userID;
						$.when(loginFB(FBID)).done(function(){
							hideLogin();
						}).fail(function(){
							showLogin();
						});
			        } else {
			        	html = $('#facebook-login').html();
			        	$('#facebook-login').html('Failed to Login. Please try again.', setTimeout( function(){
			        		$('#facebook-login').html(html);
		        		}, 3000));
			        }
	   			});
			}
		});
		$('#logout').on('click', function(){
			logout();
			showLogin();
		});
		var user = getUsername();
		$.when(getQuizzes(), getUserQuizPerformances(user)).done(function(a1, a2){

			// The a1, a2 contain the return values from the getJSON as an array with three elements
			// The [0] element contains the data
			quizItems = a1[0];
			quizperformanceItems = a2[0];

			var table = $("#quizzes tr:first");
			$.each(quizItems.items, function(key, quiz) {
				var row = $('<tr />');
				var cell = $('<td />');
				cell.append($('<a href="/startQuiz?quizID=' + quiz.quizID + '">' + quiz.name + '</a>'));
				cell.append($('<br><small>(Your progress: <span id="' + quiz.quizID + '">0</span>/' + quiz.questions + ')</small>'));
				row.append(cell);
				table.after(row);
			});

			$.each(quizperformanceItems.items, function(key, quizperf) {
				var element = $(document.getElementById(quizperf.quiz));
				element.text(quizperf.totalanswers);
			});
		});
	});
	</script>

	<%@ include file="assets/google-analytics.html"%>

</body>
</html>
