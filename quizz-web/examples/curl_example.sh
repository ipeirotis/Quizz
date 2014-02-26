#!/bin/bash

API_URL=$1 # without trailing / e.g. http://localhost:8888/_ah/api/quizz/v1
TASK_MODULE_URL=$2 # without trailing / e.g. http://localhost:50367
QUIZ_ID=$3

# create quiz
curl "$API_URL/quiz" --data "quizID=$QUIZ_ID&name=Some Quiz name for id $QUIZ_ID&text=This will be ignored and removed in future"

# add question with multiple options
curl "$API_URL/question" --header 'Content-Type: application/json; charset=utf-8' --data '{
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

# add question with free-text input
curl "$API_URL/question" --header 'Content-Type: application/json; charset=utf-8' --data '{
    "quizID": "'$QUIZ_ID'",
    "text": "Question with input text",
    "weight": 1,
    "answers": [{
        "text": "Correct answer",
        "kind": "input_text"
    }]}'


# add treatments
for TREATMENT in 'Correct' 'CrowdAnswers' 'Difficulty' 'Message' 'PercentageCorrect' 'percentageRank' 'Score' 'TotalCorrect' 'TotalCorrectRank' ; do
    curl "$API_URL/addTreatment" --data "name=$TREATMENT&probability=1."
done;

# update quiz questions count
curl "$TASK_MODULE_URL/api/getQuizCounts?quizID=$QUIZ_ID&cache=no"

# get survival probability:
curl "$API_URL/getSurvivalProbability?a_from=5&b_from=2&a_to=6&b_to=2"
