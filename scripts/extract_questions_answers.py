#!/usr/bin/python

import sys
from client import QuizzAPIClient

API_URL = 'https://crowd-power.appspot.com/'
WEB_URL = 'http://www.quizz.us/'

if len(sys.argv) < 3:
  print 'Usage: python extract_questions_answers.py [quizz_id] [num_questions]'
  exit(1)

quizz_id = sys.argv[1]
num_ques = int(sys.argv[2])

client = QuizzAPIClient(API_URL, WEB_URL)
quiz = client.get_questions_answers(quizz_id, num_ques)
for qa in quiz:
  try:
    print '\t'.join(map(unicode, qa))
  except UnicodeEncodeError as e:
    continue
