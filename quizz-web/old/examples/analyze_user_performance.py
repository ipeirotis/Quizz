#!/usr/bin/python

import json
import logging
import sys
from client import QuizzAPIClient

API_URL = 'https://crowd-power.appspot.com/'
WEB_URL = 'http://www.quizz.us/'

logging.basicConfig()
logger = logging.getLogger('analyze_user_performance')

def store_question_answers(quizz_id, num_ques, raw_file, out_file):
  ''' Fetches num_ques of questions from quizz_id, matches them up with
      questions in the quesfile, then write the output into the outfile.
      raw_file format:
          [quesText, [answers], [sub, pred, obj, probability]]
      Output format:
          [quesId, quesText, goldAnswerPosition, answers, sub, pred
           obj, probability]
          where answers is a dict from ans_pos -> (ans_text, ans_is_gold).
  '''
  # TODO: Soooo long, refactor to multiple functions?

  # PART 1: Fetches num_ques of questions from Quizz site.
  client = QuizzAPIClient(API_URL, WEB_URL)
  quiz = client.get_questions_answers(quizz_id, num_ques)

  # quesText -> quesId
  quiz_questions = dict()

  # quesText -> {answerPos -> (answerText, answerIsGold)}
  quiz_answers = dict()

  for ques in quiz:
    if len(ques) < 7: continue
    # ques format is:
    #     ['quesID', 'quesText', 'quesWeight', 'answerPos', 'answerText'
    #      'answerIsGold', 'answerProb']
    if ques[0] == 'quesID': continue
    ques_id = ques[0]
    ques_text = ques[1]
    ans_pos = ques[3]
    ans_text = ques[4]
    ans_gold = ques[5]

    if ques_text in quiz_questions and quiz_questions[ques_text] != ques_id:
      logger.warning(ques_text + ' has multiple ques_id: ' + ques_id + ', ' +
                     quiz_questions[ques_text])

    quiz_questions[ques_text] = ques_id

    if ques_text not in quiz_answers:
      quiz_answers[ques_text] = dict()
    quiz_answers[ques_text][ans_pos] = (ans_text, ans_gold)

  # PART 2: Load questions from quesfile.
  # quesText -> [sub, pred, obj, probability].
  raw_questions = dict()
  for line in open(raw_file, 'r'):
    if not line: break
    question = json.loads(line)
    # Format: [quesText, [answers], [sub, pred, obj, probability]]
    if len(question) != 3:
      logger.warning('Malformed data from ' + ques_file + ': ' + question)
      continue
    if len(question[2]) != 4:
      logger.warning('Malformed raw data from question: ' + question[2])
      continue
    raw_questions[question[0]] = question[2]

  # PART 3: Store output to out_file.
  of = open(out_file, 'w')
  for question in quiz_questions.iterkeys():
    ques_id = quiz_questions[question]
    ques_text = question
    answers = quiz_answers[question]

    gold_answers = filter(lambda x: answers[x][1], answers.iterkeys())
    if len(gold_answers) != 1:
      logger.warning('No gold answer: ' + ques_id + ', ' + ques_text + ', ' +
                     str(answers))
      continue

    if question not in raw_questions:
      logger.warning('No raw data for question: ' + ques_id + ', ' + ques_text)
      continue
    raw = raw_questions[question]
    sub = raw[0]
    pred = raw[1]
    obj = raw[2]
    prob = raw[3]

    output = [ques_id, ques_text, gold_answers[0], answers, sub, pred, obj,
              prob]
    of.write(json.dumps(output) + '\n')

  of.close()


def load_question_answers(ques_file):
  ''' Loads question/answers from ques_file and returns a data structure of the
      following format:
          quesId -> [quesText, goldAnswerPosition, answers, sub, pred
                     obj, probability]
          where answers is a dict from ans_pos -> (ans_text, ans_is_gold).

      quesfile format:
          [quesId, quesText, goldAnswerPosition, answers, sub, pred
           obj, probability]
          where answers is a dict from ans_pos -> (ans_text, ans_is_gold).
  '''
  quiz = dict()
  for line in open(ques_file, 'r'):
    if not line: break
    question = json.loads(line)
    quiz[question[0]] = question[1:]
  return quiz


def fetch_user_answers(user_ans_tsv):
  ''' user_ans_tsv is a line-delimited file, one per user answer of format:
          userid [TAB] questionID [TAB] useranswer [TAB] action [TAB] ipaddress
          [TAB] timestamp [TAB] browser [TAB] referer.
      Returns a dictionary mapping from user_id -> list of (ques_id,
          user_ans_pos, timestamp) sorted by timestamp.
  '''
  f = open(user_ans_tsv, 'r')
  user_ans = dict()
  for line in f:
    if not line: break
    tokens = line.strip().split('\t')
    if len(tokens) != 8: continue
    if tokens[0].strip() == 'userid': continue

    user_id = tokens[0].strip()
    ques_id = tokens[1].strip()
    user_ans_pos = int(tokens[2].strip())
    timestamp = int(tokens[5].strip())

    if user_id not in user_ans:
      user_ans[user_id] = []
    user_ans[user_id].append((ques_id, user_ans_pos, timestamp))

  for user_id in user_ans.iterkeys():
    new_ans = sorted(user_ans[user_id], key=lambda t: t[2])
    user_ans[user_id] = new_ans
  f.close()
  return user_ans


def compute_user_summary_stats(user_answers):
  ''' user_answers: dictionary mapping from user_id -> list of (ques_id,
          user_ans_pos, timestamp) sorted increasing by timestamp.
  '''
  num_user = 0
  num_contributing_user = 0
  num_actions = 0
  num_answers = 0
  num_answers_list = []
  for user_ans in user_answers.iterkeys():
    num_user += 1
    num_actions += len(user_answers[user_ans])
    # User answer position of -1 means 'I don't know'.
    num_ans = len(filter(lambda x: x[1] != -1, user_answers[user_ans]))
    num_answers_list.append(num_ans)
    num_answers += num_ans
    if num_ans > 0:
      num_contributing_user += 1
  num_answers_list = sorted(num_answers_list)
  answers_histogram = dict()
  for num_answer in num_answers_list:
    if num_answer not in answers_histogram:
      answers_histogram[num_answer] = 1
    else:
      answers_histogram[num_answer] += 1

  print '====================='
  print 'User summary stats:'
  print '====================='
  print 'Num user:', num_user
  print 'Num contributing user:', num_contributing_user
  print '% contributing:', float(num_contributing_user) / num_user
  print 'Total # actions:', num_actions
  print 'Total # answers:', num_answers
  print 'Avg # actions/user:', float(num_actions) / num_user
  print 'Avg # answer/user:', float(num_answers) / num_contributing_user
  print 'Median # answer/user:', num_answers_list[len(num_answers_list) / 2]
  print 'Histograms of # answers/user:'
  for ans in sorted(answers_histogram.iterkeys()):
    print ans, ': ', answers_histogram[ans]


def compute_user_accuracy_stats(user_answers, quiz, min_answers=1):
  ''' user_answers: dictionary mapping from user_id -> list of (ques_id,
          user_ans_pos, timestamp) sorted increasing by timestamp.
      quiz: quesId -> [quesText, goldAnswerPosition, answers, sub, pred
                       obj, probability]
          where answers is a dict from ans_pos -> (ans_text, ans_is_gold)
      return (gold_performance, kv_performance),
          where each performance is a dictionary of:
              user_id -> (sample_size, accuracy)
  '''
  # TODO: Switch to expected information gain.
  total_user_ans = 0
  total_user_gold_ans = 0
  total_user_correct = 0
  total_user_gold_correct = 0
  perf = []
  gold_perf = []
  perf_hist = dict()
  gold_perf_hist = dict()

  # user_id -> (sample_size, accuracy)
  gold_performance = dict()
  kv_performance = dict()

  user_answers = dict(filter(lambda x: len(x[1]) >= min_answers,
                             user_answers.iteritems()))
  print '=================================================='
  print 'User quality stats (min answers:', min_answers, ')'
  print '=================================================='

  for user in user_answers.iterkeys():
    num_user_ans = 0
    num_user_gold_ans = 0
    num_user_correct = 0
    num_user_gold_correct = 0

    for ans in user_answers[user]:
      ques_id = ans[0]
      user_ans_pos = ans[1]
      if ques_id not in quiz:
        logger.warning('Quiz does not have question: ' + ques_id)
        continue

      prob = quiz[ques_id][6]
      is_gold = prob == 0.99 or prob == 0

      # User skip or question not in quiz.
      if ques_id not in quiz:
        logger.warning(user + ' answers a ques not found, ' + ques_id)
        continue
      if user_ans_pos == -1:
        continue

      num_user_ans += 1
      if is_gold: num_user_gold_ans += 1

      gold_ans_pos = quiz[ques_id][1]
      if user_ans_pos == gold_ans_pos:
        num_user_correct += 1
        if is_gold: num_user_gold_correct += 1

      answers = quiz[ques_id][2]
      sub = quiz[ques_id][3]
      pred = quiz[ques_id][4]
      obj = quiz[ques_id][5]
      if str(user_ans_pos) not in answers:
        logger.warning('Answer position ' + str(user_ans_pos) + ' not found in '
                       + str(answers))
        continue
      if str(gold_ans_pos) not in answers:
        logger.warning('Answer position ' + str(gold_ans_pos) + ' not found in '
                       + str(answers))
        continue

    if num_user_ans == 0:
      continue

    total_user_ans += num_user_ans
    total_user_gold_ans += num_user_gold_ans
    total_user_correct += num_user_correct
    total_user_gold_correct += num_user_gold_correct

    if num_user_ans < min_answers: continue
    accuracy = num_user_correct / float(num_user_ans)
    perf.append((num_user_ans, accuracy))
    accuracy_bin = int(accuracy * 10) / float(10)
    if accuracy_bin not in perf_hist:
      perf_hist[accuracy_bin] = 0
    perf_hist[accuracy_bin] += 1
    kv_performance[user] = (num_user_ans, accuracy)

    if num_user_gold_ans < min_answers: continue
    accuracy = num_user_gold_correct / float(num_user_gold_ans)
    gold_perf.append((num_user_gold_ans, accuracy))
    accuracy_bin = int(accuracy * 10) / float(10)
    if accuracy_bin not in gold_perf_hist:
      gold_perf_hist[accuracy_bin] = 0
    gold_perf_hist[accuracy_bin] += 1
    gold_performance[user] = (num_user_gold_ans, accuracy)

  print 'Performance on gold questions:'
  print '# users', len(gold_perf)
  print 'Total gold answer', total_user_gold_ans
  print 'Total correct gold answer', total_user_gold_correct
  if total_user_gold_ans > 0:
    print 'Average gold quality', float(total_user_gold_correct) \
        / total_user_gold_ans

  if len(gold_perf) > 0:
    gold_perf = sorted(gold_perf, key=lambda x: x[1])
    print 'Median gold quality:', gold_perf[len(gold_perf)/2][1]

  total_user = sum(gold_perf_hist.itervalues())
  for p in sorted(gold_perf_hist.iterkeys()):
    print 'Accuracy:', p, ', # user:', gold_perf_hist[p], ', % user:', \
          gold_perf_hist[p] / float(total_user)
  print ''

  print 'Performance using KV answers:'
  print '# users', len(perf)
  print 'Total user answer', total_user_ans
  print 'Total correct answer', total_user_correct
  print 'Average quality', float(total_user_correct) / total_user_ans

  perf = sorted(perf, key=lambda x: x[1])
  print 'Median quality:', perf[len(perf)/2][1]

  total_user = sum(perf_hist.itervalues())
  for p in sorted(perf_hist.iterkeys()):
    print 'Accuracy:', p, ', # user:', perf_hist[p], ', % user:', \
          perf_hist[p] / float(total_user)

  return (gold_performance, kv_performance)


def compute_question_accuracy(user_answers, quiz, gold_perf, kv_perf):
  ''' user_answers: dictionary mapping from user_id -> list of (ques_id,
          user_ans_pos, timestamp) sorted increasing by timestamp.
      quiz: quesId -> [quesText, goldAnswerPosition, answers, sub, pred
                       obj, probability]
          where answers is a dict from ans_pos -> (ans_text, ans_is_gold)
      gold_perf: user_id -> (sample_size, accuracy)
      kv_perf: user_id -> (sample_size, accuracy)
  '''
  print '======================='
  print 'Question summary stats:'
  print '======================='
  # ques_id -> list of (user answer, user_gold_perf, user_kv_perf)
  ques_response = dict()
  for user_id in user_answers.iterkeys():
    for ans in user_answers[user_id]:
      ques_id = ans[0]
      ans_pos = ans[1]
      # user skips.
      if ans_pos == -1:
        continue

      if ques_id not in ques_response:
        ques_response[ques_id] = []
      user_gold_perf = -1
      user_kv_perf = -1

      if user_id in gold_perf:
        user_gold_perf = gold_perf[user_id]
      if user_id in kv_perf:
        user_kv_perf = kv_perf[user_id]

      ques_response[ques_id].append((ans_pos, user_gold_perf, user_kv_perf))

  # num response -> questions count with the given num response.
  ques_resp_hist = dict()
  for ques_id in ques_response.iterkeys():
    responses = ques_response[ques_id]
    if len(responses) not in ques_resp_hist:
      ques_resp_hist[len(responses)] = 0
    ques_resp_hist[len(responses)] += 1

  for num_response in sorted(ques_resp_hist.iterkeys()):
    print '# response:', num_response, ', # questions:', \
          ques_resp_hist[num_response]

  # List of (unweighted, gold_weighted, kv_weighted, num_ans,
  #          majority_percentage, ques_id).
  ques_quality = []
  for ques_id in ques_response.iterkeys():
    responses = ques_response[ques_id]
    unweighted = 0
    gold_weighted = 0
    kv_weighted = 0
    num_ans = 0
    num_yes = 0
    for response in responses:
      ans_pos = response[0]
      multiplier = 0
      if ans_pos == 0:
        multiplier = 1
        num_yes += 1
      elif ans_pos == 1:
        multiplier = -1
      else:
        logger.warning('Question ' + ques_id + ' is not a true/false question.')
        continue

      unweighted += multiplier
      if response[1] != -1:
        gold_weighted += response[1][1] * multiplier
      if response[2] != -1:
        kv_weighted += response[2][1] * multiplier
      num_ans += 1

    majority = 0
    if num_yes > num_ans - num_yes:
      majority = num_yes / float(num_ans)
    elif num_yes <= num_ans - num_yes:
      majority = (num_ans - num_yes) / float(num_ans)

    ques_quality.append((unweighted, gold_weighted, kv_weighted, num_ans,
                         majority, ques_id))

  votes = sorted(ques_quality, key=lambda t: abs(t[0]), reverse=True)
  print 'Num answers,', 'Majority,', 'Unweighted vote,', \
        'Weighted (gold) vote,', 'Weighted (kv) vote,', 'Sub,', 'Pred,', \
        'Obj,', 'Probability'
  for vote in votes:
    un_w = vote[0]
    gold_w = vote[1]
    kv_w = vote[2]
    num_ans = vote[3]
    majority = vote[4]
    ques_id = vote[5]

    if ques_id not in quiz: continue
    ques = quiz[ques_id]
    sub = ques[3]
    pred = ques[4]
    obj = ques[5]
    prob = ques[6]
    output = map(str, [num_ans, majority, un_w, gold_w, kv_w,
                     sub, pred, obj, prob])
    print ','.join(output)

if len(sys.argv) < 7:
  print ('Usage: python analyze_user_performance.py [user_answer_tsv] [quiz_id] '
         '[num_ques] [ques_file] [raw_file] [reload_raw_file]')
  exit(1)

user_answer_file = sys.argv[1]
quizz_id = sys.argv[2]
num_ques = int(sys.argv[3])
ques_file = sys.argv[4]
raw_file = sys.argv[5]
reload_raw_file = int(sys.argv[6])

if reload_raw_file:
  print 'Reloading'
  store_question_answers(quizz_id, num_ques, raw_file, ques_file)
quiz = load_question_answers(ques_file)
user_answers = fetch_user_answers(user_answer_file)
compute_user_summary_stats(user_answers)
#for i in xrange(2, 6):
#  compute_user_accuracy_stats(user_answers, quiz, min_answers=i)

(gold_perf, kv_perf) = compute_user_accuracy_stats(
    user_answers, quiz, min_answers=1)

compute_question_accuracy(user_answers, quiz, gold_perf, kv_perf)
