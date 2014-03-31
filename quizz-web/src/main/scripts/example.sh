#!/bin/bash

WEB_URL="crowd-power.appspot.com/" # without trailing /
API_URL="https://crowd-power.appspot.com/_ah/api/quizz/v1" # without trailing /
QUIZ_ID="testQuizId"
QUIZ_NAME="testQuizName"

echo "Creating the quiz"
curl $API_URL/insertQuiz --header 'Content-Type: application/json' --data  '{ "quizID": "'$QUIZ_ID'", "name": "'$QUIZ_NAME'", "kind": "MULTIPLE_CHOICE", "numChoices": "4"}'

#TODO: Check that the quiz was properly created
# That is is in the list of returned quizzes
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/listQuiz
# and that we can get bac 
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/getQuiz?id=testQuizId



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

echo "Updating the statistics for the Quiz"
curl $API_URL/updateQuizCounts?quizID=testQuizId

#TODO: Check that the updated numbers are ok
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/getQuiz?id=testQuizId

# Check that we get back calibration and collection questions
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/quizquestions/testQuizId

# Caching the survival probabilities to make the values available 
# We use the background tasks module, just in case it needs more time
# but this seems fast enough and often finishes within 20 secs or so.
curl quizz-tasks.$WEB_URL/api/cacheSurvivalProbability?now=true

# Caching the explore-exploit values for all combinations of values
# from a=0..10, b=0..10, c=0..5 (a=correct, b=incorrect, c=exploit)
# and for multiple choice quizzes with N=4 multiple choice options.
# This call generates a large number of individual calls (a*b*c calls)
# that are placed in an execution queue
# The results are then being used by the ProcessAnswer endpoint
# to return whether the next action is explore of exploit
curl 'quizz-tasks.$WEB_URL/api/cacheExploreExploit?a=10&b=10&c=5&N=4'

# echo "Removing the test quiz"
curl -i -H "Accept: application/json" -X DELETE $API_URL/removeQuiz?id=testQuizId



