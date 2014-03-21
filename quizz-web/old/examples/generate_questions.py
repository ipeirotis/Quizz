#!/usr/bin/python

import json
import os
import random
import sys
from sets import Set

from client import QuizzAPIClient

API_URL = 'https://crowd-power.appspot.com/'
WEB_URL = 'http://www.quizz.us/'
KV_THRESHOLD = 0.7
GOLD_ANSWER = 'selectable_gold'
WRONG_ANSWER = 'selectable_not_gold'

def convert_answer(isGold):
  if isGold: return GOLD_ANSWER
  else: return WRONG_ANSWER

class EntityNames(object):
  ''' A class to keep track of mid <-> object name.
  '''
  def __init__(self):
    self.mid_to_name = {}

  def add_mid_name(self, mid, name):
    self.mid_to_name[mid] = name

  def get_mid_name(self, mid):
    if mid in self.mid_to_name:
      return self.mid_to_name[mid]
    return mid

class EntityDisambiguation(object):
  ''' A class to keep track of mid <-> disambiguation.
  '''
  def __init__(self):
    self.mid_to_disamb = {}

  def add_mid_disamb(self, mid, disamb):
    self.mid_to_disamb[mid] = disamb

  def get_mid_disamb(self, mid):
    if mid in self.mid_to_disamb:
      return self.mid_to_disamb[mid]
    return ''

class QuestionAnswers(object):
  ''' A class to store the data structures to constructs question-answers pair.
  '''
  def __init__(self):
    # Question -> Pred -> List of Answer mids.
    self.gold_ans = {}
    # Question -> Pred -> weight.
    self.gold_weight = {}
    # Question -> Pred -> answer mid -> prob.
    self.kv_ans = {}
    # Predicate -> Set of candidate answer.
    self.candidate_ans = {}
    # Predicate -> List of candidate answers.
    self.candidate_ans_list = {}

  def add_kv_data(self, ques, pred, ans, prob):
    if ques not in self.kv_ans:
      self.kv_ans[ques] = {}
    if pred not in self.kv_ans[ques]:
      self.kv_ans[ques][pred] = {}
    if ans not in self.kv_ans[ques][pred]:
      self.kv_ans[ques][pred][ans] = prob
    for key in self.kv_ans[ques][pred].iterkeys():
      if key == ans and self.kv_ans[ques][pred][key] < prob:
        self.kv_ans[ques][pred][key] = prob
        return

  def add_gold_data(self, ques, pred, ans, weight):
    if ques not in self.gold_ans:
      self.gold_ans[ques] = {}
      self.gold_weight[ques] = {}
    if pred not in self.gold_ans[ques]:
      self.gold_ans[ques][pred] = []
    self.gold_ans[ques][pred].append(ans)
    self.gold_weight[ques][pred] = [weight]
    if pred not in self.candidate_ans:
      self.candidate_ans[pred] = Set([])
    self.candidate_ans[pred].add(ans)

  def print_stats(self):
    gold_count = 0
    for ques in self.gold_ans.iterkeys():
      for pred in self.gold_ans[ques].iterkeys():
        gold_count = gold_count + len(self.gold_ans[ques][pred])
    print '# gold questions:', len(self.gold_ans)
    print '# gold answers:', gold_count

    kv_count = 0
    for ques in self.kv_ans.iterkeys():
      for pred in self.kv_ans[ques].iterkeys():
        kv_count = kv_count + len(self.kv_ans[ques][pred])
    print '# kv answers:', kv_count

    candidate_count = 0
    for pred in self.candidate_ans.iterkeys():
      candidate_count = candidate_count + len(self.candidate_ans[pred])
    print '# candidate answers:', candidate_count

  def candidate_size(self):
    candidate_count = 0
    for pred in self.candidate_ans.iterkeys():
      candidate_count = candidate_count + len(self.candidate_ans[pred])
    return candidate_count

  def question_list(self):
    kp_ans = Set(self.gold_ans.iterkeys())
    kv_ans = Set(self.kv_ans.iterkeys())
    return kp_ans.intersection(kv_ans)

  def init_generate_question(self):
    if len(self.candidate_ans_list) != len(self.candidate_ans):
      for pred in self.candidate_ans.iterkeys():
        self.candidate_ans_list[pred] = list(self.candidate_ans[pred])

  def sample_wrong_ans(self, num_wrong_ans, ques, pred, potential_ans):
    ''' Sample num_wrong_ans from candidate answers for the given ques without
        using the golden answers for the ques and the potential_ans.
    '''
    wrong_ans = []
    if (pred not in self.candidate_ans_list or
        pred not in self.gold_ans[ques]):
      return wrong_ans
    candidate_ans = self.candidate_ans_list[pred]
    candidate_count = len(candidate_ans)
    for i in xrange(0, num_wrong_ans):
      trial = 0
      while trial < 100:  # Probably not enough candidate ans.
        trial = trial + 1
        rand_ind = random.randint(0, candidate_count - 1)
        rand_ans = candidate_ans[rand_ind]
        if (rand_ans not in potential_ans and
            rand_ans not in self.gold_ans[ques][pred] and
            rand_ans not in wrong_ans):
          wrong_ans.append(rand_ans)
          break
      if trial == 100:
        break
    return wrong_ans

  def generate_kp_question(self, key, mid_names, collection=''):
    ''' Generates questions from KP using the following idea:
        1. Take one golden answer for the question key.
        2. Randomly choose wrong answers from the candidate list (same expected type)
           as long as the wrong answer is not a gold answer or a high confidence answer. 
    '''
    results = []
    num_wrong_ans = 3
    for pred in self.gold_ans[key].iterkeys():
      if pred not in self.kv_ans[key]:
        continue

      kv_mids = []
      for kv_ans in self.kv_ans[key][pred].iterkeys():
        if self.kv_ans[key][pred][kv_ans] >= KV_THRESHOLD:
          kv_mids.append(kv_ans);

      for ans in self.gold_ans[key][pred]:
        question = mid_names.get_mid_name(key)
        right_ans = mid_names.get_mid_name(ans)
        weight = self.gold_weight[key][pred]
        wrong_ans = self.sample_wrong_ans(num_wrong_ans, key, pred, kv_mids)

        if len(wrong_ans) == num_wrong_ans:
          json_ans = wrong_ans
          json_ans.append(ans)
          random.shuffle(json_ans)
          answers = [{'text': mid_names.get_mid_name(answer), 'kind': convert_answer(answer == ans)}
                     for answer in json_ans]
          if collection != '':
            question = question + ' (' + collection + ')'
          results.append((question, pred, answers, weight, 'Golden'))
    return results

  def generate_kv_question(self, key, mid_names, collection):
    ''' Generates questions from KV using the following idea:
        1. Pick the most confidence answer from KV as the golden answer.
        2. Pick random answers from the rest of the KV answers or the candidate answers
           with expected type.
        3. Add a new answer 'None of the above'
        4. The final golden answer is either the most confident answer from KV or
           'None of the above' if the most confident answer has less confidence than
           the kv threshold.
    '''
    results = []
    for pred in self.kv_ans[key].iterkeys():
      if pred not in self.gold_ans[key]:
        continue

      queue = []
      kv_mids = []
      for kv_ans in self.kv_ans[key][pred].iterkeys():
        if kv_ans not in self.gold_ans[key][pred]:
          queue.append((self.kv_ans[key][pred][kv_ans], kv_ans))
          kv_mids.append(kv_ans)

      queue = sorted(queue, key=lambda item: item[0], reverse=True)
      wrong_ans = self.sample_wrong_ans(3, key, pred, kv_mids)
      for ans in wrong_ans:
        queue.append((0, ans))
      if len(queue) < 4:
        continue
      queue = queue[:4]
      # there is probably no answer if the top prob is less than KV_THRESHOLD
      item = queue[0]
      is_none_answer = item[0] < KV_THRESHOLD

      question = mid_names.get_mid_name(key)
      weight = self.gold_weight[key][pred]
      answers = []
      if item[0] >= KV_THRESHOLD:
        answers = [{'text': mid_names.get_mid_name(item[1]),
                    'kind': GOLD_ANSWER}]
      for i in xrange(1, 4):
        item = queue[i]
        answers.append({'text': mid_names.get_mid_name(item[1]),
                        'kind': WRONG_ANSWER,
                        'probability': item[0],
                        'source': 'KV'})
      random.shuffle(answers)
      prob = 0
      if is_none_answer:
        answers.append({'text': 'None of the option',
                        'kind': GOLD_ANSWER})
      else:
        answers.append({'text': 'None of the option',
                        'kind': WRONG_ANSWER,
                        'probability': prob,
                        'source': 'KV'})
      results.append((question, pred, answers, weight, 'KV'))
    return results

class QuestionGenerator(object):
  def __init__(self, ques_template_file):
    # Create Quizz client.
    TREATMENTS = ['Correct', 'CrowdAnswers', 'Difficulty', 'Message',
                  'PercentageCorrect', 'percentageRank', 'Score', 'TotalCorrect',
                  'TotalCorrectRank']
    self.client = QuizzAPIClient(API_URL, WEB_URL)
    for treatment in TREATMENTS:
      self.client.add_treatment(treatment, 0.8)
    self.qa_dict = QuestionAnswers()
    self.mid_names = EntityNames()

    self.ques_templates = {}
    inpf = open(ques_template_file, "r")
    for line in inpf:
      if not line: break
      tokens = line.strip().split("\t")
      if len(tokens) != 2:
        continue
      pred = tokens[0]
      ques_template = tokens[1]
      self.ques_templates[pred] = ques_template
    inpf.close()

  def create_quiz(self, quizz_id, quizz_name):
    self.client.create_quiz(quizz_id, quizz_name)
    self.quizz_id = quizz_id
    self.quizz_name = quizz_name

  def parse_kp_data(self, tokens):
    ''' Format of tokens:
        question, question_name, pred, answer, answer_name, weight
    '''
    if len(tokens) != 6: return
    q = tokens[0]
    q_name = tokens[1]
    pred = tokens[2]
    a = tokens[3]
    a_name = tokens[4]
    weight = tokens[5]
    self.mid_names.add_mid_name(q, q_name)
    self.mid_names.add_mid_name(a, a_name)
    self.qa_dict.add_gold_data(q, pred, a, weight)

  def parse_kv_data(self, tokens):
    ''' Format of tokens:
        question, pred, answer, answer_name, probability, belief system.
    '''
    if len(tokens) != 6: return
    q = tokens[0]
    pred = tokens[1]
    a = tokens[2]
    a_name = tokens[3]
    prob = float(tokens[4])
    belief_system = tokens[5]
    self.qa_dict.add_kv_data(q, pred, a, prob);
    self.mid_names.add_mid_name(a, a_name)

  def print_stats(self):
    print self.quizz_id, 'stats:'
    self.qa_dict.print_stats()

  def load_quizz_data(self, file_path, quizz_id, collection):
    inpf = open(file_path + quizz_id + '-qa_pair.csv', 'r')
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
        self.parse_kv_data(tokens)
      elif is_qa:
        self.parse_kp_data(tokens)
    inpf.close()
    self.collection_name = collection

  def generate_questions(self, generation_function):
    self.qa_dict.init_generate_question()
    questions = []
    for key in self.qa_dict.question_list():
      questions = questions + generation_function(key, self.mid_names, self.collection_name)
    return questions

  def add_question(self, qa):
    (question, pred, answers, weight, source) = qa
    q_text = self.ques_templates[pred].replace('XXX', question)
    self.client.add_question(self.quizz_id, q_text, answers, weight, source, False)

  def add_questions(self, questions):
    for qa in questions:
      self.add_question(qa)
    self.client.update_count_stats()
    print '# questions:', len(questions)

  def generate_kp_questions(self, num_questions=-1):
    questions = self.generate_questions(self.qa_dict.generate_kp_question)
    if num_questions == -1:
      self.add_questions(questions)
      return

    num_questions = min(len(questions), num_questions)

    # Sample about the same number of questions per predicates.
    final_questions = []
    preds = dict()
    for question in questions:
      pred = question[1]
      if pred not in preds:
        preds[pred] = 0
      preds[pred] += 1
    random.shuffle(questions)
    count = 0
    ques_per_pred = float(num_questions) / float(len(preds))
    for question in questions:
      pred = question[1]
      rate = ques_per_pred / float(preds[pred])
      if random.random() < rate:
        print json.dumps(question)
        final_questions.append(question)
        count += 1
      if count >= num_questions:
        break

    self.add_questions(final_questions)

  def generate_kv_questions(self):
    questions = self.generate_questions(self.qa_dict.generate_kv_question)
    self.add_questions(questions)

  def hash_question(self, question):
    ques = question[0]
    pred = question[1]
    return ques + "\t" + pred

  def generate_kp_and_kv_questions(self, is_kp=True, is_kv=True):
    num_kp = 0
    num_kv = 0
    kp_questions = self.generate_questions(self.qa_dict.generate_kp_question)
    kv_questions = self.generate_questions(self.qa_dict.generate_kv_question)

    questions = Set(map(lambda x: self.hash_question(x), kp_questions)).intersection(
                Set(map(lambda x: self.hash_question(x), kv_questions)))

    remaining_questions = []
    if is_kp:
      kp_done_question = Set()
      for question in kp_questions:
        question_hash = self.hash_question(question)
        if question_hash in questions and question_hash not in kp_done_question:
          remaining_questions.append(question)
          kp_done_question.add(question_hash)
          num_kp = num_kp + 1
    if is_kv:
      kv_done_question = Set()
      for question in kv_questions:
        question_hash = self.hash_question(question)
        if question_hash in questions and question_hash not in kv_done_question:
          remaining_questions.append(question)
          kv_done_question.add(question_hash)
          num_kv = num_kv + 1

    random.shuffle(remaining_questions)
    self.add_questions(remaining_questions)

    print '# KP questions', num_kp
    print '# KV questions', num_kv

  def set_quizz_id(self, quizz_id):
    self.quizz_id = quizz_id

class KVQuestionGenerator(object):
  def __init__(self, ques_template_file):
    # Create Quizz client.
    TREATMENTS = ['Correct', 'CrowdAnswers', 'Difficulty', 'Message',
                  'PercentageCorrect', 'percentageRank', 'Score', 'TotalCorrect',
                  'TotalCorrectRank']
    self.client = QuizzAPIClient(API_URL, WEB_URL)
    for treatment in TREATMENTS:
      self.client.add_treatment(treatment, 0.8)

    self.ques_templates = {}
    inpf = open(ques_template_file, "r")
    for line in inpf:
      if not line: break
      tokens = line.strip().split("\t")
      if len(tokens) != 2:
        continue
      pred = tokens[0]
      ques_template = tokens[1]
      self.ques_templates[pred] = ques_template
    inpf.close()

    # mid -> pred -> answer -> prob (0.99)
    self.correct_answers = dict()
    # mid -> pred -> answer -> prob (0)
    self.wrong_answers = dict()
    # mid -> pred -> answer -> prob.
    self.kv_answers = dict()
    self.mid_names = EntityNames()
    self.mid_disamb = EntityDisambiguation()

  def create_quiz(self, quizz_id, quizz_name):
    self.client.create_quiz(quizz_id, quizz_name)
    self.quizz_id = quizz_id
    self.quizz_name = quizz_name

  def add_kv_answer(self, answers, sub, pred, answer, prob):
    if sub not in answers:
      answers[sub] = dict()
    if pred not in answers[sub]:
      answers[sub][pred] = dict()
    answers[sub][pred][answer] = prob

  def parse_data(self, tokens, collection):
    if len(tokens) != 7: return

    sub = tokens[0]
    sub_name = tokens[1]
    pred = tokens[2]
    obj = tokens[3]
    obj_name = tokens[4]
    prob = tokens[5]
    source = tokens[6]
    if float(prob) == 0:
      self.add_kv_answer(self.wrong_answers, sub, pred, obj, 0)
    elif float(prob) == 0.99:
      self.add_kv_answer(self.correct_answers, sub, pred, obj, 0.99)
    else:
      self.add_kv_answer(self.kv_answers, sub, pred, obj, float(prob))
    self.mid_names.add_mid_name(sub, sub_name)
    self.mid_names.add_mid_name(obj, obj_name)
    self.mid_disamb.add_mid_disamb(sub, collection)

  def load_questions(self, ques_dir):
    for f in os.listdir(ques_dir):
      collection = f.replace('-qa_pair.csv', '')
      file_name = ques_dir + '/' + f
      is_golden = False
      is_kv = False
      for line in open(file_name, 'r'):
        if not line: break
        tokens = line.strip().split(',')
        if tokens[0] == 'question':
          continue
        self.parse_data(tokens, collection)

  def sample_questions(self, answers, num_questions=100):
    questions = []
    for mid in answers.iterkeys():
      for pred in answers[mid].iterkeys():
        for obj in answers[mid][pred].iterkeys():
          question = [mid, pred, obj, answers[mid][pred][obj]]
          questions.append(question)
    random.shuffle(questions)
    num_questions = min(len(questions), num_questions)
    questions = questions[:num_questions]
    return questions

  def generate_questions(self, num_correct, num_wrong, num_kv):
    all_questions = []
    all_questions.extend(self.sample_questions(self.correct_answers, num_correct))
    all_questions.extend(self.sample_questions(self.wrong_answers, num_wrong))
    all_questions.extend(self.sample_questions(self.kv_answers, num_kv))
    random.shuffle(all_questions)

    for question in all_questions:
      sub = question[0]
      sub_name = self.mid_names.get_mid_name(sub)
      sub_disamb = self.mid_disamb.get_mid_disamb(sub).replace('_', ' ')
      pred = question[1]
      obj = question[2]
      prob = question[3]
      obj_name = self.mid_names.get_mid_name(obj)
      if sub_name == '' or sub_disamb == '' or obj_name == '':
        continue
      q_text = self.ques_templates[pred].replace('XXX', sub_name + ' (' + sub_disamb + ')')
      q_text = q_text.replace('YYY', obj_name)
      answers = [{'text': 'Yes', 'kind': convert_answer(prob >= KV_THRESHOLD)},
                 {'text': 'No', 'kind': convert_answer(prob < KV_THRESHOLD)}]
      final_question = [q_text, answers, question]
      print json.dumps(final_question)
      self.client.add_question(self.quizz_id, q_text, answers)

    self.client.update_count_stats()

def merge_questions(path_dir, num_questions, ques_template, quizz_id, quizz_name, filter_pred=''):
  ''' Merge all questions found in path_dir/* and choose num_questions from
      them to be submitted to Quizz with the given quizz_id and quizz_name.
      If filter_pred is not empty, keep only questions where predicate = given filter_pred.
  '''
  questions = []
  for f in os.listdir(path_dir):
    file_name = path_dir + '/' + f
    for line in open(file_name, 'r'):
      if not line: break
      question = json.loads(line)
      if filter_pred != '' and question[1] != filter_pred:
        continue
      questions.append(question)
  random.shuffle(questions)
  num_questions = min(len(questions), num_questions)

  questions = questions[:num_questions]
  for question in questions:
    print json.dumps(question)

def upload_question(quiz_id, quiz_name, file_path):
  kg_generator = QuestionGenerator('../data/question_template.txt')
  kg_generator.set_quizz_id(quiz_id)
  kg_generator.create_quiz(quiz_id, quiz_name)
  questions = []
  for line in open(file_path, 'r'):
    if not line: break
    questions.append(json.loads(line))
  kg_generator.add_questions(questions)

if len(sys.argv) < 6:
  print 'Usage: python generate_question.py [quizz_id] [quizz_name] [ques_template_file] [file_path] [is_control_kp_kv]'
  print 'ques_template_file: A file with mapping from kp predicate to question template'
  exit(1)

quizz_id = sys.argv[1]
quizz_name = sys.argv[2]
ques_template = sys.argv[3]
file_path = sys.argv[4]
is_control_kp_kv = bool(sys.argv[5])

kv_generator = KVQuestionGenerator(ques_template)
kv_generator.create_quiz(quizz_id, quizz_name)
kv_generator.load_questions(file_path)
kv_generator.generate_questions(50, 50, 400)

#kg_generator = QuestionGenerator(ques_template)
#kg_generator.load_quizz_data(file_path, quizz_id, quizz_name)
#kg_generator.generate_kp_questions(100)
