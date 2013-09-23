<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:include page="/header.jsp"></jsp:include>

<body>
    <div class="container" style="text-align: center; max-width:720px">

        <h2>
            <a href="/"><span style="color: maroon">Quizz</span>.us</a>
        </h2>


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
    </div>

    <script>
    $(document).ready(function() {
        setUpPerformanceUpdatesChannel(getURLParameterByName('changelToken'));
        var user = getUsername();
        var quiz = getURLParameterByName('relation');
        var gclid = getURLParameterByName('gclid');
        $('#gclid').val(gclid);

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
