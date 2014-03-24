#!/bin/bash

API_URL="https://crowd-power.appspot.com/_ah/api/quizz/v1" # without trailing /
QUIZ_ID="testQuizId"
QUIZ_NAME="testQuizName"

echo "Creating the quiz"
curl $API_URL/insertQuiz --header 'Content-Type: application/json' --data  '{ "quizID": "'$QUIZ_ID'", "name": "'$QUIZ_NAME'", "kind": "MULTIPLE_CHOICE", "numChoices": "4"}'

#TODO: Check that the quiz was properly created

for i in {1..10}
do
   echo "Adding calibration question #$i"
#   curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data  '{ "quizID": "'$QUIZ_ID'", "text": "Calibration Question '$i'", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Answer 1 - gold", "kind": "GOLD" }, { "text": "Answer 2", "kind": "INCORRECT" }, { "text": "Answer 3", "kind": "INCORRECT" }, { "text": "Answer 4", "kind": "INCORRECT" }] }'
done

for i in {1..10}
do
   echo "Adding collection question #$i"
#   curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data  '{ "quizID": "'$QUIZ_ID'", "text": "Collection Question '$i'", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Answer 1 - silver", "kind": "SILVER" }, { "text": "Answer 2", "kind": "INCORRECT" }, { "text": "Answer 3", "kind": "INCORRECT" }, { "text": "Answer 4", "kind": "INCORRECT" }] }'
done

#TODO: The updateQuizCounts should be a POST call, not a GET
echo "Updating the statistics for the Quiz"
curl $API_URL/updateQuizCounts?quizID=testQuizId

#TODO: Check that the updated numbers are ok

# echo "Removing the test quiz"
curl -i -H "Accept: application/json" -X DELETE $API_URL/removeQuiz?id=testQuizId

