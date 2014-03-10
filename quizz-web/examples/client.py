import json
import requests


def GoodUrl(url):
  if not url.endswith("/"):
    url += "/"
  return url


def ParseJsonResponse(response):
  text = response.text
  try:
    return json.loads(text)
  except ValueError as ve:
    print text
    raise ve


class QuizzAPIClient(object):
  """ A wrapper class around Quizz cloud endpoints.
  """
  def __init__(self, api_url, web_url, auth=None):
    self.session = requests.Session()
    self.api_url = GoodUrl(api_url) + "_ah/api/quizz/v1/"
    self.web_url = GoodUrl(web_url)
    if auth:
      self.session.auth = auth

  def _Req(self, fun, *args, **kwargs):
    resp = fun(*args, **kwargs)
    return resp

  def _Get(self, url, data):
    return self._Req(self.session.get, url, params=data)

  def _Post(self, url, data, header={}):
    return self._Req(self.session.post, url, headers=header, data=data)

  def _Delete(self, url, data):
    return self._Req(self.session.delete, url, params=data)

  def _GetWeb(self, sub_path, data={}):
    return self._Get(self.web_url + sub_path, data)

  def _GetApi(self, sub_path, data={}):
    return self._Get(self.api_url + sub_path, data)

  def _PostApi(self, sub_path, data={}):
    return self._Post(self.api_url + sub_path, data)

  def _PostApiJson(self, sub_path, data={}):
    headers = {
      'Content-Type' : 'application/json',
      'charset' : 'utf-8'
    }
    return self._Post(self.api_url + sub_path, json.dumps(data), headers)

  def _DeleteApi(self, sub_path, data={}):
    return self._Delete(self.api_url + sub_path, data)

  def CreateQuiz(self, quiz_id, name):
    """ Creates a new Quiz entity with the given quiz_id and name.

    This replaces the old Quiz entity with the same quiz_id if one
    exists.
    """
    data = {
      'quizID': quiz_id,
      'name': name,
    }
    return self._PostApi('addQuiz', data).text

  def ListQuizes(self):
    """ Lists all the Quiz entities in the datastore.
    """
    jresp = ParseJsonResponse(self._GetApi("listQuiz"))
    return jresp.get('items', [])

  def ListQuizesId(self):
    """ Lists all the quiz_ids of Quiz entities in the datastore.
    """
    quizes_list = self.ListQuizes()
    return map(lambda q: q['quizID'], quizes_list)

  def GetQuiz(self, quiz_id):
    """ Returns the Quiz entity given the quiz_id.
    """
    return ParseJsonResponse(self._GetApi('getQuiz', {'id' : quiz_id}))

  def RemoveQuiz(self, quiz_id):
    """ Removes the Quiz entity given the quiz_id.
    """
    return self._DeleteApi('removeQuiz', {'id': quiz_id})

  def AddQuestion(self, quiz_id, text, answers, weight=1.):
    """ Adds a new Question entity to the datastore for the given quiz_id.

    Args:
      answers: A list of Answer entities, with each Answer has at least the
               "text" and the "kind" field set. "kind" field could be any of
               "selectable_gold", "selectable_non_gold", "silver", "input_text".
      Note: Each gold question must have at least one answer's kind be either
            "input_text" or "selectable_gold". Each silver (collection) question
            must have at least one answer's kind be equal to "silver".
    """
    data = {
      'quizID': quiz_id,
      'text': text,
      'weight': weight,
      'answers': answers,
    }
    return ParseJsonResponse(self._PostApiJson('insertQuestion', data))

  def GetQuestions(self, quizID, amount):
    """ Gets amount number of questions from quiz with quizID given.
    """
    return ParseJsonResponse(self._GetApi("quizquestions/" + quizID,
        {'num' : amount}))

  def UpdateCountStats(self):
    """ Updates the stats on each quiz based on the associated questions.

    Note: This needs to be called after each new quiz creation and
          questions addition to allow some other endpoints to work properly.
          Alternatively, a cron job runs every 5 minutes to update the stats.
          TODO(chunhowt): This is not ideal and should be fixed.
    """
    return self._GetWeb("api/updateCountStatistics")
