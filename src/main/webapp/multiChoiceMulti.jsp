<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:include page="/header.jsp"></jsp:include>

<body>
    <div class="container" style="text-align: center; max-width:720px">

        <jsp:include page="/logo.jsp"></jsp:include>

        <div id="scores" class="alert alert-info" style="text-align: center;">
            <div id="inScores">
                <span class="label label-info" id="showScore"></span>
                <span class="label label-info" id="showTotalCorrect"></span>
                <span class="label label-info" id="showPercentageCorrect"></span>
                <span class="label label-info"  id="showPercentageRank"></span>
                <span class="label label-info" id="showTotalCorrectRank"></span>
            </div>
            <div id="scoresLoading">
                <img src="assets/horizontal_loader.gif"/>
            </div>
        </div>

        <div id="form" class="well" style="text-align: center;">
            <div id="loadingScreen" style="display: none;">
                <h3 id="loadingMsg"></h3>
                <img src="assets/round_loader.gif" style="padding-top: 30px; margin-bottom: 30px;"/>
            </div>
            <div id="feedback" style="padding-bottom: 10px;"></div>
            <form id="addUserEntry">
                <fieldset>
                    <div class="lead">
                        <span id="questiontext"></span>
                        <a id="midname" href=""></a>
                    </div>
                    <div id="answers"></div>
                    <input id="quizID" name="quizID" type="hidden" value="">
                    <input id="questionID" name="questionID" type="hidden" value="">
                    <input id="totalanswers" name="totalanswers" type="hidden" value="">
                    <input id="correctanswers" name="correctanswers" type="hidden" value="">
                    <input id="gclid" name="gclid" type="hidden" value="">
                </fieldset>
            </form>
        <span id="questionsPackProgress"></span>
        </div>
        <div id="quizEndSummary" style="display: none;">
            <!-- It will be moved (using JS) into form -->
            <h3>Thank you for completing quizz!</h3>
            <p>You have answered correctly for <span id="correctCountSummary"></span>
            out of <span id="totalCountSummary"></span> questions.</p>

            <div id="sharingButtonsBox">

            <h4>Share your results:</h4>
                <table width="50%" height="100%" align="center" valign="center">
                    <tr><td valign="top">
                        <a class="btn btn-primary" href="#"
                          onclick="
                            window.open('http://www.facebook.com/sharer/sharer.php?s=100' +
                            		'&p[url]=' + encodeURIComponent('http://www.quizz.us') +
                            		'&p[images][0]=' +
                            		'&p[title]=I+have+just+answered+' + CORRECT_COUNT + '+out+of+' + TOTAL_COUNT + '+questions+correctly!',
                              'facebook-share-dialog',
                              'width=626,height=436');
                            return false;">
                          Facebook
                        </a>
                    </td><td>
                        <a href="https://twitter.com/share" class="twitter-share-button" data-url="http://www.quizz.us" data-text="I have just answered CORRECT_COUNT out of TOTAL_COUNT questions correctly!" data-size="large" data-count="none">Tweet</a>
                        <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script>
                    </td><td>
                        <!-- Place this tag where you want the share button to render. -->
                        <div class="g-plus" data-action="share" data-annotation="none" data-height="24" data-href="http://www.quizz.us"></div>
                    </td></tr>
                </table>
            </div>


            <h3><a id="restartQuizzLink" href="/startQuiz?quizID=">Clik here to start again</a></h3>
        </div>
    </div>

    <script>
    $(document).ready(function() {
        setUpPerformanceUpdatesChannel(getURLParameterByName('changelToken'));
        var user = getUsername();
        var quiz = getURLParameterByName('quizID');
        var gclid = getURLParameterByName('gclid');
        $('#gclid').val(gclid);
        $('#restartQuizzLink').attr('href', $('#restartQuizzLink').attr('href') + quiz);

        hideDivs();
        makeLoadingScreen("Loading questions");

        $.when(
            getNextQuizQuestions(quiz),
            getUserTreatments(user),
            getUserQuizPerformance(quiz, user)
        ).done( function (questions, userTreatments, userQuizPerformance) {
            initializeQuizzWithQuestions(fst(questions));
            applyTreatments(fst(userTreatments));
            displayPerformanceScores(fst(userQuizPerformance));
            disableLoadingScreen();
        });

    });
    </script>

<%@ include file="assets/google-analytics.html" %>


</body>
</html>
