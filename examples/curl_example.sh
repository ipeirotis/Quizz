#!/bin/bash

WEB_URL=$1 # without trailing /
QUIZ_ID=$2

# create quiz
curl "$WEB_URL/addQuiz" --data "quizID=$QUIZ_ID&name=Some Quiz name for id $QUIZ_ID&text=This will be ignored and removed in future"

# add question with multiple options
curl "$WEB_URL/addQuestion" --data '{
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
curl "$WEB_URL/addQuestion" --data '{
    "quizID": "'$QUIZ_ID'",
    "text": "Question with input text",
    "weight": 1,
    "answers": [{
        "text": "Correct answer",
        "kind": "input_text"
    }]}'


# add treatments
for TREATMENT in 'Correct' 'CrowdAnswers' 'Difficulty' 'Message' 'PercentageCorrect' 'percentageRank' 'Score' 'TotalCorrect' 'TotalCorrectRank' ; do
    curl "$WEB_URL/addTreatment" --data "name=$TREATMENT&probability=1."
done;

# update quiz questions count, two ways
# update all quizzes:
curl "$WEB_URL/api/updateCountStatistics"

# update only given one:
curl "$WEB_URL/api/getQuizCounts?quizID=$QUIZ_ID&cache=no"

