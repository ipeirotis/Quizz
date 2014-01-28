#!/bin/bash

WEB_URL=$1 # without trailing /
QUIZ_ID=$2

# create ad
curl "$WEB_URL/campaignManagement" --data "quizID=$QUIZ_ID&budget=1&cpcid=1&keywords=test&adheadline=test_headline&adline1=test_line_1&adline2=test_line_2"
