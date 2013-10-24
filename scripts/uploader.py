import csv
import json
import sys

import requests


API_URL = 'http://localhost:8888/'
WEB_URL = 'http://localhost:8888/'


def good_url(url):
    if not url.endswith("/"):
        url += "/"
    return url


def J(response):
    return json.loads(response.text)


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

    def add_question(self, quiz_id, text, answers, weight=1.):
        data = {
            'quizID': quiz_id,
            'text': text,
            'weight': weight,
            'answers': answers,
        }
        return J(self._post_web('addQuestion', json.dumps(data)))

    def add_answer(self, questionID, answer, **kwargs):
        kwargs['text'] = answer
        kwargs['questionID'] = questionID
        return self._post_web("addAnswer", json.dumps(kwargs))

    def update_count_stats(self):
        return self._get_web("api/updateCountStatistics")

    def get_questions(self, quizID, amount):
        return J(self._get_api("quizquestions/" + quizID, {'num': amount}))

    def add_treatment(self, name, probability):
        return self._post_web('addTreatment',
                             {'name': name, 'probability': probability})


def load_questions(fname):
    with open(fname) as F:
        csvfile = csv.reader(F, delimiter='\t')
        for row in csvfile:
            (text, gold), answers = row[:2], row[2:]
            yield text, gold, answers


def upload_questions(client, quiz_id, questions):
    for text, gold, answers in questions:
        answers = [{'text': answer, 'isGold': (answer == gold)}
                   for answer in answers]
        client.add_question(quiz_id, text, answers)


TREATMENTS = ['Correct', 'CrowdAnswers', 'Difficulty', 'Message',
              'PercentageCorrect', 'percentageRank', 'Score', 'TotalCorrect',
              'TotalCorrectRank']


def main(args):
    client = QuizzAPIClient(API_URL, WEB_URL)
    fname, quiz_id, quizz_text = args[:3]
    for treatment in TREATMENTS:
        client.add_treatment(treatment, 0.8)
    client.create_quiz(quiz_id, quizz_text)
    questions = load_questions(fname)
    upload_questions(client, quiz_id, questions)
    client.update_count_stats()


if __name__ == '__main__':
    main(sys.argv[1:])
