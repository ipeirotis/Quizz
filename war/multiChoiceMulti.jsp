<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:include page="/header.jsp"></jsp:include>

<body>
    <div class="container" style="text-align: center; max-width:720px">

        <h2>
            <a href="/"><span style="color: maroon">Quizz</span>.us</a>
        </h2>

        <div id="feedback"></div>

        <div id="scores" class="alert alert-info" style="text-align: center;">
            <span class="label label-info" id="showScore"></span>
            <span class="label label-info" id="showTotalCorrect"></span>
            <span class="label label-info" id="showPercentageCorrect"></span>
            <span class="label label-info"  id="showPercentageRank"></span>
            <span class="label label-info" id="showTotalCorrectRank"></span>
        </div>

        <div id="form" class="well" style="text-align: center;">
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


        var user = getUsername();
        var quiz = getURLParameterByName('relation');
        var mid = getURLParameterByName('mid');
        var prior = getURLParameterByName('prior');
        var gclid = getURLParameterByName('gclid');
        $('#gclid').val(gclid);

        getNextQuizQuestions(quiz);

        if (prior) {
            var feedbackdiv = $('#feedback');
            feedbackdiv.append($('<div id="showMessage"></div>'));
            feedbackdiv.append($('<div class="alert alert-success" id="showCorrect"></div>'));
            feedbackdiv.append($('<div class="alert alert-info" id="showCrowdAnswers"></div>'));
            getFeedbackForPriorAnswer(user, quiz, prior);
        }

        hideDivs();

        getUserTreatments(user);
        getUserQuizPerformance(quiz, user);

        if (prior) {
            $('#feedback').show();
            $('#feedback').delay(5000).fadeOut(600);
        }
        $('#scores').show();
        $('#form').show();


    });
    </script>

<%@ include file="assets/google-analytics.html" %>


</body>
</html>
