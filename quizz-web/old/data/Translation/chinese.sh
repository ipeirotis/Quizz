#!/bin/bash

WEB_URL='http://www.quizz.us' # without trailing /
QUIZ_ID='translateChinese'



# create quiz
curl $WEB_URL/addQuiz --data "quizID=$QUIZ_ID&name=Translate in Chinese&text=ignored"

IFS=","
i=1
while read f1 f2 f3 f4
do
    test $i -eq 1 && ((i=i+1)) && continue
    [ -z "$f1" ] && continue

    echo "==================="
    echo "Entry #$i"
    echo "english is   : $f1"
    echo "chinese is   : $f2"
    echo "mid is       : $f3"
    echo "frequency is : $f4"

data="{ \"quizID\": $QUIZ_ID, \"text\": \"How would you say <a target=\\\"_blank\\\" href=\\\"http://freebase.com$f3\\\">$f1</a> in Chinese?\", \"weight\": 1, \"answers\": [{ \"text\": \"$f2\", \"kind\": \"input_text\", \"isGold\": true }]}"

curl $WEB_URL/addQuestion --data "$data" --header 'Content-Type: application/x-www-form-urlencoded; charset=utf-8'

i=`expr $i + 1`

done < chinese.csv

curl "$WEB_URL/api/getQuizCounts?quizID=$QUIZ_ID&cache=no"
