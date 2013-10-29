#!/usr/bin/python

import json
import random
import sys
from sets import Set

from client import QuizzAPIClient

API_URL = 'https://crowd-power.appspot.com/'
WEB_URL = 'http://www.quizz.us/'

if len(sys.argv) < 3:
  print 'Usage: python generate_question.py [quizz_id] [quizz_name] [ques_template]'
  print 'ques_template: Some string question with XXX to be filled by the question entity'
  exit(1)

quizz_id = sys.argv[1]
quizz_name = sys.argv[2]
ques_template = sys.argv[3]
inpf = open('../data/kg_questions/' + quizz_id + '-qa_pair.csv', 'r')
#outf = open('kg_questions/' + quizz_id + '-quiz.csv', 'w')

## ================================================
# Initializes data structure for questions-answers.
## ================================================
# Dictionary from question to list of answers.
gold_ans = {}
# Dictionary from question to weight of question.
gold_weight = {}
# Dictionary from mid to object name.
mid_to_name = {}
# Dictionary from question to hash set of potential answers.
kv_ans = {}
# Answers with right expected type
candidate_ans = Set([])

is_qa = False
is_kv = False
for line in inpf:
  if not line: break
  tokens = line.strip().split(',')
  if tokens[0] == 'question':
    if not is_qa:
      is_qa = True
    else:
      is_kv = True
    continue

  if is_kv:
    if len(tokens) != 4: continue
    q = tokens[0]
    a = tokens[1]
    prob = float(tokens[2])
    belief_system = tokens[3]

    # Update kv candidate answer table.
    if prob > 0.5:
      if q in kv_ans:
        kv_ans[q].add(a)
      else:
        kv_ans[q] = Set([a])
  elif is_qa:
    if len(tokens) != 5: continue
    q = tokens[0]
    q_name = tokens[1]
    a = tokens[2]
    a_name = tokens[3]
    weight = tokens[4]

    # Update entity names mapping table.
    if q not in mid_to_name:
      mid_to_name[q] = q_name
    if a not in mid_to_name:
      mid_to_name[a] = a_name

    # Update golden answer table.
    if q not in gold_ans:
      gold_ans[q] = [a]
      gold_weight[q] = weight
    else:
      gold_ans[q].append(a)
    candidate_ans.add(a)

## ===========
# Print stats.
## ===========
print quizz_id, 'stats:'
gold_count = 0
for key in gold_ans.iterkeys():
  gold_count = gold_count + len(gold_ans[key])
print '# gold questions:', len(gold_ans)
print '# gold answers:', gold_count

kv_count = 0
for key in kv_ans.iterkeys():
  kv_count = kv_count + len(kv_ans[key])
print '# kv answers:', kv_count

# Merge in gold answers to kv answers as well.
for key in gold_ans.iterkeys():
  if key in kv_ans:
    kv_ans[key].update(Set(gold_ans[key]))
  else:
    kv_ans[key] = Set(gold_ans[key])

candidate_ans = list(candidate_ans)
candidate_count = len(candidate_ans)
print '# candidate answers:', candidate_count


# Create Quizz client.
TREATMENTS = ['Correct', 'CrowdAnswers', 'Difficulty', 'Message',
              'PercentageCorrect', 'percentageRank', 'Score', 'TotalCorrect',
              'TotalCorrectRank']
client = QuizzAPIClient(API_URL, WEB_URL)
for treatment in TREATMENTS:
  client.add_treatment(treatment, 0.8)
client.create_quiz(quizz_id, quizz_name)

## ==================
# Generate questions.
## ==================
num_questions = 0
num_wrong_ans = 3  # Change the rest too if change this!!
for key in gold_ans.iterkeys():
  for ans in gold_ans[key]:
    question = mid_to_name[key]
    right_ans = mid_to_name[ans]
    weight = gold_weight[key]
    wrong_ans = []
    # Choose 3 other answer choices.
    for i in xrange(0, num_wrong_ans):
      trial = 0
      while trial < 100:  # Probably not enough candidate ans.
        trial = trial + 1
        rand_ind = random.randint(0, candidate_count - 1)
        rand_ans = candidate_ans[rand_ind]
        if rand_ans not in kv_ans[key] and mid_to_name[rand_ans] not in wrong_ans:
          wrong_ans.append(mid_to_name[rand_ans])
          break
      if trial == 100:
        break
    if len(wrong_ans) == num_wrong_ans:
      question = ques_template.replace('XXX', question)

      json_ans = wrong_ans
      json_ans.append(right_ans)
      random.shuffle(json_ans)

      if i >= 0:
        answers = [{'text': answer, 'isGold': (answer == right_ans)}
                   for answer in json_ans]
        client.add_question(quizz_id, question, answers, weight, False)
        #json_ques = {'question': question, 'correct_ans_ind': correct_ans_index, 'answers': json_ans}
        #json.dump(json_ques, outf)
        num_questions = num_questions + 1

client.update_count_stats()
print '# questions:', num_questions
inpf.close()
#outf.close()
