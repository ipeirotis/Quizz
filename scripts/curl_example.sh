#!/bin/bash

WEB_URL=$1 # without trailing /
QUIZ_ID=$2

# create quiz
curl "$WEB_URL/addQuiz" --data "quizID=$QUIZ_ID&name=Some Quiz name for id $QUIZ_ID&text=This will be ignored and removed in future"

# add question
curl "$WEB_URL/addQuestion" --data '{
    "quizID": "'$QUIZ_ID'",
    "text": "Question text goes here",
    "weight": 1,
    "answers": [{
        "text": "Answer 1",
        "isGold": false
    }, {
        "text": "Answer 2 - it is gold one but do not tell anyone",
        "isGold": true
    }]}'

# update quiz questions count, two ways
# update all quizzes:
curl "$WEB_URL/api/updateCountStatistics"

# update only given one:
curl "$WEB_URL/api/getQuizCounts?quizID=$QUIZ_ID&cache=no"

