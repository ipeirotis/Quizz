#!/usr/bin/python

import subprocess
import sys
import os

filename = 'quizz_collection_template.txt'
dremel_script = 'dremel.collection'
if len(sys.argv) == 3:
  filename = sys.argv[1]
  dremel_script = sys.argv[2]

tmpl = open(filename, 'r')
p = None
for line in tmpl:
  if not line: break

  if line[0] == '#': continue

  tokens = line.strip().split('\t')
  tokens = map(lambda x: x.strip('"'), tokens)
  dremel_args = ['./generate_candidate.sh', tokens[0], tokens[1], tokens[2], tokens[3], dremel_script]
  p = subprocess.Popen(dremel_args)
  p.wait()
  ques_args = ['../scripts/generate_questions.py', tokens[2], tokens[4], 'question_template.txt', 'True']
  p = subprocess.Popen(ques_args)
tmpl.close()

if p:
  p.wait()
