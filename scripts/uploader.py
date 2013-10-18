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
        print resp
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

    def add_question(self, quiz_id, text, weight=1.):
        data = {
            'quizID': quiz_id,
            'text': text,
            'weight': weight,
        }
        return self._post_web('addQuestion', data).text

    def add_answer(self, answer):
        pass


def main(args):
    client = QuizzAPIClient(API_URL, WEB_URL)
    fname, quiz_id, quizz_text = args[:3]
    client.create_quiz(quiz_id, quizz_text)


if __name__ == '__main__':
    main(sys.argv[1:])
