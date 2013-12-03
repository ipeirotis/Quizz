#!/usr/bin/python

import subprocess
import sys
import os

filename = 'quizz_collection_template.txt'
dremel_script = 'dremel.collection'
output_dir = 'collection_questions'
if len(sys.argv) == 4:
  filename = sys.argv[1]
  dremel_script = sys.argv[2]
  output_dir = sys.argv[3]

tmpl = open(filename, 'r')
p = None
for line in tmpl:
  if not line: break

  if line[0] == '#': continue

  tokens = line.strip().split('\t')
  tokens = map(lambda x: x.strip('"'), tokens)
  collection = tokens[0]
  predicate_file = tokens[1]
  quizz_id = tokens[2]
  question_count = tokens[3]
  quizz_name = tokens[4]

  dremel_args = ['./generate_candidate.sh', collection, predicate_file, quizz_id, question_count, dremel_script, output_dir]
  p = subprocess.Popen(dremel_args)
  p.wait()
  ques_args = ['../scripts/generate_questions.py', quizz_id, quizz_name, 'question_template.txt', '../data/' + output_dir + '/', 'True']
  p = subprocess.Popen(ques_args)
tmpl.close()

if p:
  p.wait()
