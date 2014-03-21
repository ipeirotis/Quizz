import csv
import sys

from client import QuizzAPIClient


API_URL = 'http://localhost:8888/'
WEB_URL = 'http://localhost:8888/'

API_URL = 'https://crowd-power.appspot.com/'
WEB_URL = 'http://www.quizz.us/'


def load_questions(fname):
    with open(fname) as F:
        ANSWERS = [x + " of them" for x in ["None", "Some", "All"]]
        csvfile = csv.reader(F, delimiter='\t')
        for row in csvfile:
            (quiz_id, quiz_name), (text, gold) = row[1:3], row[8:10]
            #print quiz_id, quiz_name, text, gold
            if not gold:
                continue
            quiz_id = quiz_id.replace("/", "_")
            yield quiz_id, quiz_name, text, gold, ANSWERS


def ensure_quiz(quizzes, client, quiz_id, quiz_name):
    if quiz_id not in quizzes:
        client.create_quiz(quiz_id, quiz_name)
        quizzes.add(quiz_id)


def upload_questions(client, questions):
    quizzes = set()
    for quiz_id, quiz_name, text, gold, answers in questions:
        ensure_quiz(quizzes, client, quiz_id, quiz_name)
        answers = [{'text': answer, 'isGold': (answer == gold)}
                   for answer in answers]
        client.add_question(quiz_id, text, answers)


TREATMENTS = ['Correct', 'CrowdAnswers', 'Difficulty', 'Message',
              'PercentageCorrect', 'percentageRank', 'Score', 'TotalCorrect',
              'TotalCorrectRank']


def main(args):
    client = QuizzAPIClient(API_URL, WEB_URL)
    fname = args[0]
    for treatment in TREATMENTS:
        client.add_treatment(treatment, 0.8)
    questions = load_questions(fname)
    upload_questions(client, questions)
    client.update_count_stats()


if __name__ == '__main__':
    main(sys.argv[1:])
