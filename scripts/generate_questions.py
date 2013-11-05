#!/usr/bin/python

import json
import Queue
import random
import sys
from sets import Set

from client import QuizzAPIClient

API_URL = 'https://crowd-power.appspot.com/'
WEB_URL = 'http://www.quizz.us/'
KV_THRESHOLD = 0.5

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

class QuestionAnswers(object):
  ''' A class to store the data structures to constructs question-answers pair.
  '''
  def __init__(self):
    # Question -> List of Answer mids.
    self.gold_ans = {}
    # Question -> weight.
    self.gold_weight = {}
    # Question -> Dictionary of (answer mid -> prob)
    self.kv_ans = {}
    # Set of all answer mids for this type.
    self.candidate_ans = Set([])
    self.candidate_ans_list = []

  def add_kv_data(self, ques, ans, prob):
    if ques not in self.kv_ans:
      self.kv_ans[ques] = {}
    if ans not in self.kv_ans[ques]:
      self.kv_ans[ques][ans] = prob
    for key in self.kv_ans[ques].iterkeys():
      if key == ans and self.kv_ans[ques][key] < prob:
        self.kv_ans[ques][key] = prob
        return

  def add_gold_data(self, ques, ans, weight):
    if ques not in self.gold_ans:
      self.gold_ans[ques] = []
    self.gold_ans[ques].append(ans)
    self.gold_weight[ques] = [weight]
    self.candidate_ans.add(ans)

  def print_stats(self):
    gold_count = 0
    for key in self.gold_ans.iterkeys():
      gold_count = gold_count + len(self.gold_ans[key])
    print '# gold questions:', len(self.gold_ans)
    print '# gold answers:', gold_count

    kv_count = 0
    for key in self.kv_ans.iterkeys():
      kv_count = kv_count + len(self.kv_ans[key])
    print '# kv answers:', kv_count

    candidate_count = len(self.candidate_ans)
    print '# candidate answers:', candidate_count

  def candidate_size(self):
    return len(self.candidate_ans)

  def question_list(self):
    kp_ans = Set(self.gold_ans.iterkeys())
    kv_ans = Set(self.kv_ans.iterkeys())
    return kp_ans.intersection(kv_ans)

  def init_generate_question(self):
    if len(self.candidate_ans_list) != len(self.candidate_ans):
      self.candidate_ans_list = list(self.candidate_ans)

  def sample_wrong_ans(self, num_wrong_ans, ques, potential_ans):
    ''' Sample num_wrong_ans from candidate answers for the given ques without
        using the golden answers for the ques and the potential_ans.
    '''
    wrong_ans = []
    candidate_count = self.candidate_size()
    for i in xrange(0, num_wrong_ans):
      trial = 0
      while trial < 100:  # Probably not enough candidate ans.
        trial = trial + 1
        rand_ind = random.randint(0, candidate_count - 1)
        rand_ans = self.candidate_ans_list[rand_ind]
        if (rand_ans not in potential_ans and
            rand_ans not in self.gold_ans[ques] and
            rand_ans not in wrong_ans):
          wrong_ans.append(rand_ans)
          break
      if trial == 100:
        break
    return wrong_ans

  def generate_kp_question(self, key, mid_names):
    ''' Generates questions from KP using the following idea:
        1. Take one golden answer for the question key.
        2. Randomly choose wrong answers from the candidate list (same expected type)
           as long as the wrong answer is not a gold answer or a high confidence answer. 
    '''
    results = []
    num_wrong_ans = 3
    kv_mids = []
    for kv_ans in self.kv_ans[key].iterkeys():
      if self.kv_ans[key][kv_ans] >= KV_THRESHOLD:
        kv_mids.append(kv_ans);

    for ans in self.gold_ans[key]:
      question = mid_names.get_mid_name(key)
      right_ans = mid_names.get_mid_name(ans)
      weight = self.gold_weight[key]
      wrong_ans = self.sample_wrong_ans(num_wrong_ans, key, kv_mids)

      if len(wrong_ans) == num_wrong_ans:
        json_ans = wrong_ans
        json_ans.append(ans)
        random.shuffle(json_ans)

        answers = [{'text': mid_names.get_mid_name(answer), 'isGold': (answer == ans)}
                   for answer in json_ans]
        results.append((question, answers, weight, 'Golden'))
    return results

  def generate_kv_question(self, key, mid_names):
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
    queue = []
    kv_mids = []
    for kv_ans in self.kv_ans[key].iterkeys():
      if kv_ans not in self.gold_ans[key]:
        queue.append((self.kv_ans[key][kv_ans], kv_ans))
        kv_mids.append(kv_ans)

    queue = sorted(queue, key=lambda item: item[0], reverse=True)
    wrong_ans = self.sample_wrong_ans(3, key, kv_mids)
    for ans in wrong_ans:
      queue.append((0, ans))
    if len(queue) < 4:
      return results
    queue = queue[:4]
    # there is probably no answer if the top prob is less than KV_THRESHOLD
    item = queue[0]
    is_none_answer = item[0] < KV_THRESHOLD

    question = mid_names.get_mid_name(key)
    weight = self.gold_weight[key]
    answers = []
    if item[0] >= KV_THRESHOLD:
      answers = [{'text': mid_names.get_mid_name(item[1]),
                  'isGold': item[0] >= KV_THRESHOLD}]
    for i in xrange(1, 4):
      item = queue[i]
      answers.append({'text': mid_names.get_mid_name(item[1]),
                      'isGold': False,
                      'probability': item[0],
                      'source': 'KV'})
    random.shuffle(answers)
    prob = 0
    if is_none_answer:
      answers.append({'text': 'None of the option',
                      'isGold': True})
    else:
      answers.append({'text': 'None of the option',
                      'isGold': False,
                      'probability': prob,
                      'source': 'KV'})
    results.append((question, answers, weight, 'KV'))
    return results

class QuestionGenerator(object):
  def __init__(self, quizz_id, quizz_name, ques_template):
    self.quizz_id = quizz_id
    self.quizz_name = quizz_name
    self.ques_template = ques_template
    # Create Quizz client.
    TREATMENTS = ['Correct', 'CrowdAnswers', 'Difficulty', 'Message',
                  'PercentageCorrect', 'percentageRank', 'Score', 'TotalCorrect',
                  'TotalCorrectRank']
    self.client = QuizzAPIClient(API_URL, WEB_URL)
    for treatment in TREATMENTS:
      self.client.add_treatment(treatment, 0.8)
    self.client.create_quiz(self.quizz_id, self.quizz_name)
    self.qa_dict = QuestionAnswers()
    self.mid_names = EntityNames()

  def parse_kp_data(self, tokens):
    ''' Format of tokens:
        question, question_name, answer, answer_name, weight
    '''
    if len(tokens) != 5: return
    q = tokens[0]
    q_name = tokens[1]
    a = tokens[2]
    a_name = tokens[3]
    weight = tokens[4]
    self.mid_names.add_mid_name(q, q_name)
    self.mid_names.add_mid_name(a, a_name)
    self.qa_dict.add_gold_data(q, a, weight)

  def parse_kv_data(self, tokens):
    ''' Format of tokens:
        question, answer, answer_name, probability, belief system.
    '''
    if len(tokens) != 5: return
    q = tokens[0]
    a = tokens[1]
    a_name = tokens[2]
    prob = float(tokens[3])
    belief_system = tokens[4]
    self.qa_dict.add_kv_data(q, a, prob);
    self.mid_names.add_mid_name(a, a_name)

  def print_stats(self):
    print self.quizz_id, 'stats:'
    self.qa_dict.print_stats()

  def load_quizz_data(self):
    inpf = open('../data/kg_questions/' + self.quizz_id + '-qa_pair.csv', 'r')
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

  def generate_questions(self, generation_function):
    self.qa_dict.init_generate_question()
    questions = []
    for key in self.qa_dict.question_list():
      questions = questions + generation_function(key, self.mid_names)
    return questions

  def add_question(self, qa):
    (question, answers, weight, source) = qa
    q_text = self.ques_template.replace('XXX', question)
    self.client.add_question(self.quizz_id, q_text, answers, weight, source, False)

  def add_questions(self, questions):
    for qa in questions:
      self.add_question(qa)
    self.client.update_count_stats()
    print '# questions:', len(questions)

  def generate_kp_questions(self):
    questions = self.generate_questions(self.qa_dict.generate_kp_question)
    self.add_questions(questions)

  def generate_kv_questions(self):
    questions = self.generate_questions(self.qa_dict.generate_kv_question)
    self.add_questions(questions)

  def generate_kp_and_kv_questions(self, is_kp=True, is_kv=True):
    num_kp = 0
    num_kv = 0
    kp_questions = self.generate_questions(self.qa_dict.generate_kp_question)
    kv_questions = self.generate_questions(self.qa_dict.generate_kv_question)

    questions = Set(map(lambda x: x[0], kp_questions)).intersection(Set(map(lambda x: x[0], kv_questions)))

    remaining_questions = []
    if is_kp:
      kp_done_question = Set()
      for question in kp_questions:
        if question[0] in questions and question[0] not in kp_done_question:
          remaining_questions.append(question)
          kp_done_question.add(question[0])
          num_kp = num_kp + 1
    if is_kv:
      kv_done_question = Set()
      for question in kv_questions:
        if question[0] in questions and question[0] not in kv_done_question:
          remaining_questions.append(question)
          kv_done_question.add(question[0])
          num_kv = num_kv + 1

    random.shuffle(remaining_questions)
    self.add_questions(remaining_questions)

    print '# KP questions', num_kp
    print '# KV questions', num_kv

if len(sys.argv) < 3:
  print 'Usage: python generate_question.py [quizz_id] [quizz_name] [ques_template]'
  print 'ques_template: Some string question with XXX to be filled by the question entity'
  exit(1)

quizz_id = sys.argv[1]
quizz_name = sys.argv[2]
ques_template = sys.argv[3]

kg_generator = QuestionGenerator(quizz_id, quizz_name, ques_template)
kg_generator.load_quizz_data()
kg_generator.generate_kp_and_kv_questions(True, True)
