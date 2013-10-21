import time
import unittest
import ipdb

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

    def load_questions(self):
        for text, answers, gold in QUESTIONS:
            question_resp = self.client.add_question(self.quiz_id, text)
            question_id = question_resp['questionID']
            for answer in answers:
                self.client.add_answer(answer,
                        questionID=question_id,
                        isGold=(answer == gold))

    def wait(self, amount=0.25):
        time.sleep(amount)

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

    def test_add_question_gold_and_not(self):
        questions = self.client.get_questions(self.quiz_id, 1)
        self.assertTrue('error' in questions)
        self.load_questions()
        self.wait()
        questions = self.client.get_questions(self.quiz_id, 1)
        # ^^ 1 because we are only taking questions with gold
        self.assertTrue('error' not in questions)
        self.assertTrue('items' in questions)
        questions = self.client.get_questions(self.quiz_id, 2)
        self.assertTrue('error' in questions)

    def test_get_question(self):
        self.load_questions()
        self.wait()
        questions = self.client.get_questions(self.quiz_id, 1)
        question = questions['items'][0]
        self.assertEqual(question['text'], QUESTIONS[0][0])


if __name__ == '__main__':
    unittest.main()
