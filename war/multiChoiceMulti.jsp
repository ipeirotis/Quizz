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
                    <input id="relation" name="relation" type="hidden" value="">
                    <input id="mid" name="mid" type="hidden" value="">
                    <input id="totalanswers" name="totalanswers" type="hidden" value="">
                    <input id="correctanswers" name="correctanswers" type="hidden" value="">
                    <input id="gold" name="gold" type="hidden" value="">
                    <input id="gclid" name="gclid" type="hidden" value="">
                </fieldset>
            </form>
        <span id="questionsPackProgress"></span>
        </div>
        <div id="quizEndSummary" style="display: none;">
            <!-- It will be moved (using JS) into form -->
            <h3>Thank you for completing quizz!</h3>
            You have answered correctly for <span id="correctCountSummary"></span>
            out of <span id="totalCountSummary"></span> questions.

            Share your results:

            <h3><a id="restartQuizzLink" href="/startQuiz?relation=">Clik here to start again</a></h3>
        </div>
    </div>

    <script>
    $(document).ready(function() {
        setUpPerformanceUpdatesChannel(getURLParameterByName('changelToken'));
        var user = getUsername();
        var quiz = getURLParameterByName('relation');
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
