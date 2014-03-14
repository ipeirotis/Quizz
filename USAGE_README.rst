
USING QUIZZ
===========


Uploading Data
~~~~~~~~~~~~~~

We create quizzes using the Quizz API. (While we used to have a web page for
uploading data, we have deprecated it.)

This is an example of how to create a quiz::

  $ curl https://crowd-power.appspot.com/_ah/api/quizz/v1/addQuiz --data 'quizID=$QUIZ_ID&name=$QUIZ_NAME'

Each quiz contains questions. Each question can be of two types:

a. A multiple choice question

b. A free-text entry question

Here is an example of creating a multiple choice entry gold question::

  $ curl https://crowd-power.appspot.com/_ah/api/quizz/v1/insertQuestion --header 'Content-Type: application/json' --data '{
        "quizID": "'$QUIZ_ID'",
        "text": "Question text goes here",
        "weight": 1,
        "answers": [{
            "text": "Answer 1",
            "kind": "selectable_not_gold"
        }, {
            "text": "Answer 2 - it is gold one but do not tell anyone",
            "kind": "selectable_gold"
        }]}'

Here is an example of creating a multiple choice entry silver question::

  $ curl https://crowd-power.appspot.com/_ah/api/quizz/v1/insertQuestion --header 'Content-Type: application/json' --data '{
        "quizID": "'$QUIZ_ID'",
        "text": "Question text goes here",
        "weight": 1,
        "answers": [{
            "text": "Answer 1",
            "kind": "selectable_not_gold"
        }, {
            "text": "Answer 2 - it is silver but do not tell anyone",
            "kind": "silver"
        }]}'

Here is an example of creating a free-text entry. You will notice that we
can put arbitrary HTML code in the question, and that we also upload a (set of)
answers that are correct. (All answers with "input_text" kind are considered
correct)::

  $ curl https://crowd-power.appspot.com/_ah/api/quizz/v1/insertQuestion --header 'Content-Type: application/json; charset=utf-8' --data '{
        "quizID": "'$QUIZ_ID'",
        "text": "How would you say <a target=\"_blank\" href=\"http://freebase.com/m/07ylj‎\">Venezuela</a> in Greek?",
        "weight": 1,
        "answers": [{
            "text": "Βενεζουέλα",
            "kind": "input_text",
            "isGold": true
        }, {
            "text": "Μπολιβαριανή Δημοκρατία της Βενεζουέλας",
            "kind": "input_text",
            "isGold": true
        }
        ]}'

After uploading all the data, we also need to update the internal counters for
Quizz (since we want to have a very responsive interface, we try to do a lot of
computations 'offline') so that we can serve the quiz. Else, some of the
endpoints won't work. So we call::

  $ curl https://crowd-power.appspot.com/_ah/api/quizz/v1/updateQuizCounts?quizID=$QUIZ_ID


Creating Ad Campaign
~~~~~~~~~~~~~~~~~~~~

TODO

