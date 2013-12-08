#!/bin/bash

WEB_URL='http://www.quizz.us' # without trailing /
QUIZ_ID='translateRussian2'



# create quiz
curl $WEB_URL/addQuiz --data "quizID=$QUIZ_ID&name=Translate into Russian&text=ignored"

IFS=","
i=1
while read f1 f2 f3 f4
do
    test $i -eq 1 && ((i=i+1)) && continue
    [ -z "$f1" ] && continue

    echo "==================="
    echo "Entry #$i"
    echo "english is   : $f1"
    echo "russian is   : $f2"
    echo "mid is       : $f3"
    echo "frequency is : $f4"

data="{ \"quizID\": $QUIZ_ID, \"text\": \"Как сказать по-русски <a target=\\\"_blank\\\" href=\\\"http://freebase.com$f3\\\">$f1</a>?\", \"weight\": 1, \"answers\": [{ \"text\": \"$f2\", \"kind\": \"input_text\", \"isGold\": true }]}"

curl $WEB_URL/addQuestion --data "$data" --header 'Content-Type: application/x-www-form-urlencoded; charset=utf-8'

i=`expr $i + 1`

done < russian.csv

curl "$WEB_URL/api/getQuizCounts?quizID=$QUIZ_ID&cache=no"
