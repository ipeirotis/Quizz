#!/bin/bash

WEB_URL="http://crowd-power.appspot.com" # without trailing /
QUIZ_ID="testQuizId"
QUIZ_NAME="testQuizName"

echo "Creating the quiz"
curl https://crowd-power.appspot.com/_ah/api/quizz/v1/addQuiz --data “quizID=testQuizId&name=testQuizName&kind=MULTIPLE_CHOICE”

#TODO: Check that the quiz was properly created

for i in {1..10}
do
   echo "Adding calibration question #$i"
   curl https://crowd-power.appspot.com/_ah/api/quizz/v1/insertQuestion --header 'Content-Type: application/json' --data  '{ "quizID": "testQuizId", "text": "testQuestion$i", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Answer 1 - gold", "kind": "selectable_gold" }, { "text": "Answer 2", "kind": "selectable_not_gold" }, { "text": "Answer 3", "kind": "selectable_not_gold" }, { "text": "Answer 4", "kind": "selectable_not_gold" }] }' 
done

for i in {1..10}
do
   echo "Adding collection question #$i"
   curl https://crowd-power.appspot.com/_ah/api/quizz/v1/insertQuestion --header 'Content-Type: application/json' --data  '{ "quizID": "testQuizId", "text": "testQuestion$i", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Answer 1", "kind": "selectable_gold" }, { "text": "Answer 2", "kind": "selectable_not_gold" }, { "text": "Answer 3", "kind": "selectable_not_gold" }, { "text": "Answer 4", "kind": "selectable_not_gold" }] }' 
done

#TODO: The updateQuizCounts should be a POST call, not a GET
echo "Updating the statistics for the Quiz"
curl https://crowd-power.appspot.com/_ah/api/quizz/v1/updateQuizCounts?quizID=testQuizId

#TODO: Check that the updated numbers are ok
