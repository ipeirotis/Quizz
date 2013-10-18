import time
import unittest

from uploader import QuizzAPIClient


API_URL = 'http://localhost:8888/'
WEB_URL = 'http://localhost:8888/'


QUESTIONS = [
    ("1st question text", ["answer 1", "answer2", "answ3"], "answer2"),
    ("2nd question text", ["a1", "a2", "a3", "a4", "a5", "a6"], None)
]


class DataUploadTests(unittest.TestCase):

    def setUp(self):
        self.client = QuizzAPIClient(API_URL, WEB_URL)
        self.quiz_id = "PY_TEST_QUIZ"
        self.client.create_quiz(self.quiz_id, "PY TEST QUIZ")

    def tearDown(self):
        self.client.remove_quiz(self.quiz_id)

    def wait(self):
        time.sleep(1)

    def test_add_quiz(self):
        self.wait()
        quizes = self.client.list_quizes_id()
        self.assertTrue(self.quiz_id in quizes)

    def test_remove_quiz(self):
        self.wait()
        self.client.remove_quiz(self.quiz_id)
        self.wait()
        quizes = self.client.list_quizes_id()
        self.assertTrue(self.quiz_id not in quizes)

    def test_add_question(self):
        pass


if __name__ == '__main__':
    unittest.main()
