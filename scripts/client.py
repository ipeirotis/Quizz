import json

import requests


def good_url(url):
    if not url.endswith("/"):
        url += "/"
    return url


def J(response):
    text = response.text
    try:
        return json.loads(text)
    except ValueError as ve:
        print text
        raise ve


class QuizzAPIClient(object):

    def __init__(self, api_url, web_url, auth=None):
        self.session = requests.Session()
        self.api_url = good_url(api_url) + "_ah/api/quizz/v1/"
        self.web_url = good_url(web_url)
        if auth:
            self.session.auth = auth

    def _req(self, fun, *args, **kwargs):
        resp = fun(*args, **kwargs)
        return resp

    def _get(self, url, data):
        return self._req(self.session.get, url, params=data)

    def _post(self, url, data):
        return self._req(self.session.post, url, data=data)

    def _get_web(self, sub_path, data={}):
        return self._get(self.web_url + sub_path, data)

    def _get_api(self, sub_path, data={}):
        return self._get(self.api_url + sub_path, data)

    def _post_api(self, sub_path, data={}):
        return self._post(self.api_url + sub_path, data)

    def _post_web(self, sub_path, data={}):
        return self._post(self.web_url + sub_path, data)

    def create_quiz(self, quiz_id, name):
        data = {
            'quizID': quiz_id,
            'name': name,
            'text': "I don't know .. " + name,
        }
        return self._post_web('addQuiz', data).text

    def list_quizes(self):
        jresp = J(self._get_api("quiz",))
        return jresp.get('items', [])

    def list_quizes_id(self):
        quizes_list = self.list_quizes()
        return map(lambda q: q['quizID'], quizes_list)

    def remove_quiz(self, quiz_id):
        return self._get_web('api/deleteQuiz', {'quizID': quiz_id})

    def add_question(self, quiz_id, text, answers, weight=1., source='', print_debug=True):
        data = {
            'quizID': quiz_id,
            'text': text,
            'weight': weight,
            'answers': answers,
            'source': source,
        }
        if print_debug:
          print data
        return J(self._post_web('addQuestion', json.dumps(data)))

    def add_answer(self, questionID, answer, **kwargs):
        kwargs['text'] = answer
        kwargs['questionID'] = questionID
        return self._post_web("addAnswer", json.dumps(kwargs))

    def update_count_stats(self):
        return self._get_web("api/updateCountStatistics")

    def get_questions(self, quizID, amount):
        return J(self._get_api("quizquestions/" + quizID, {'num': amount}))

    def get_questions_answers(self, quizID, amount):
        results = self.get_questions(quizID, amount)
        questions = results['items']
        qas = [['quesID', 'quesText', 'quesWeight', 'answerPos',
                'answerText', 'answerIsGold', 'answerProb']]
        for question in questions:
          if ('id' not in question or
              'text' not in question or
              'weight' not in question or
              'answers' not in question):
            continue
          quesID = question['id']
          quesText = question['text']
          quesWeight = question['weight']
          answers = question['answers']

          for answer in answers:
            if 'internalID' not in answer or 'text' not in answer:
              continue
            answerPos = answer['internalID']
            answerText = answer['text']
            answerIsGold = False
            answerProb = 0.0
            if 'isGold' in answer:
              answerIsGold = answer['isGold']
              answerProb = 1.0
            elif 'silver' in answer and 'probability' in answer and answer['silver']:
              answerIsGold = False
              answerProb = answer['probability']
            else:
              continue
            qas.append([quesID, quesText, quesWeight, answerPos,
                        answerText, answerIsGold, answerProb])
        return qas

    def add_treatment(self, name, probability):
        return self._post_web('addTreatment',
                             {'name': name, 'probability': probability})
