package us.quizz.repository;

import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.UserAnswer;
import us.quizz.utils.CachePMF;
import us.quizz.utils.Helper;
import us.quizz.utils.MemcacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class QuizQuestionRepository extends BaseRepository<Question> {
  // Multiplier used to fetch more questions than being asked when choosing the next questions
  // to allow us to randomly choose a subset of the candidate questions.
  private static final int QUESTION_FETCHING_MULTIPLIER = 5;

  // Number of seconds to cache the quiz questions.
  private static final int QUESTIONS_CACHED_TIME_SECONDS = 5 * 60;  // 5 minutes.

  QuizRepository quizRepository;

  @Inject
  public QuizQuestionRepository(QuizRepository quizRepository) {
    super(Question.class);
    this.quizRepository = quizRepository;
  }

  @Override
  protected Key getKey(Question item) {
    return item.getKey();
  }

  public Map<String, Set<Question>> getNextQuizQuestionsWithoutCaching(String quizID, int n) {
    Map<String, Set<Question>> result = new HashMap<String, Set<Question>>();

    int N = n * QUESTION_FETCHING_MULTIPLIER;
    ArrayList<Question> goldQuestions = getSomeQuizQuestionsWithGold(quizID, N);
    result.put("gold", Helper.trySelectingRandomElements(goldQuestions, n));
    ArrayList<Question> silverQuestions = getSomeQuizQuestionsWithSilver(quizID, N);
    result.put("silver", Helper.trySelectingRandomElements(silverQuestions, n));

    String key = MemcacheKey.getQuizQuestionsByQuiz(quizID, n);
    int cached_lifetime = QUESTIONS_CACHED_TIME_SECONDS;
    CachePMF.put(key, result, cached_lifetime);
    return result;
  }

  public Map<String, Set<Question>> getNextQuizQuestions(String quizID, int n) {
    String key = MemcacheKey.getQuizQuestionsByQuiz(quizID, n);
    @SuppressWarnings("unchecked")
    Map<String, Set<Question>> result = CachePMF.get(key, Map.class);
    if (result != null) {
      return result;
    } else {
      return getNextQuizQuestionsWithoutCaching(quizID, n);
    }
  }

  public Question getQuizQuestion(String questionID) {
    return getQuizQuestion(Long.parseLong(questionID));
  }

  public Question getQuizQuestion(Long questionID) {
    return get(questionID);
  }

  protected Query getQuestionBaseQuery(PersistenceManager pm) {
    Query query = pm.newQuery(Question.class);
    query.getFetchPlan().setFetchSize(1000);
    return query;
  }

  protected Query getQuestionQuery(PersistenceManager pm,
      String filter, String declaredParameters) {
    Query query = getQuestionBaseQuery(pm);
    query.setFilter(filter);
    query.declareParameters(declaredParameters);
    return query;
  }

  public ArrayList<Question> getQuizQuestions() {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = getQuestionBaseQuery(pm);
      @SuppressWarnings("unchecked")
      List<Question> results = (List<Question>) query.execute();
      return new ArrayList<Question>(results);
    } finally {
      pm.close();
    }
  }

  protected ArrayList<Question> getQuestions(String filter,
      String declaredParameters, Map<String, Object> params) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = getQuestionQuery(pm, filter, declaredParameters);

      @SuppressWarnings("unchecked")
      ArrayList<Question> questions = new ArrayList<Question>(
          (List<Question>) query.executeWithMap(params));
      return questions;
    } finally {
      pm.close();
    }
  }

  protected ArrayList<Question> getQuestionsWithCaching(String key,
      String filter, String declaredParameters, Map<String, Object> params) {
    @SuppressWarnings("unchecked")
    ArrayList<Question> questions = CachePMF.get(key, ArrayList.class);
    if (questions != null) {
      return questions;
    }
    questions = getQuestions(filter, declaredParameters, params);
    CachePMF.put(key, questions);
    return questions;
  }

  public ArrayList<Question> getQuizQuestions(String quizid) {
    String key = MemcacheKey.getAllQuizQuestionsByQuiz(quizid);
    String filter = "quizID == quizParam";
    String declaredParameters = "String quizParam";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizParam", quizid);
    return getQuestionsWithCaching(key, filter, declaredParameters, params);
  }

  protected Query getQuizGoldQuestionsQuery(PersistenceManager pm, String quizID) {
    String filter = "quizID == quizParam && hasGoldAnswer == hasGoldParam";
    String declaredParameters = "String quizParam, Boolean hasGoldParam";
    return getQuestionQuery(pm, filter, declaredParameters);
  }

  protected Map<String, Object> getQuizGoldQuestionsParameters(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizParam", quizID);
    params.put("hasGoldParam", Boolean.TRUE);
    return params;
  }

  protected Query getQuizSilverQuestionsQuery(PersistenceManager pm, String quizID) {
    String filter = "quizID == quizParam && hasSilverAnswers == hasSilverParam";
    String declaredParameters = "String quizParam, Boolean hasSilverParam";
    return getQuestionQuery(pm, filter, declaredParameters);
  }

  protected Map<String, Object> getQuizSilverQuestionsParameters(String quizID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizParam", quizID);
    params.put("hasSilverParam", Boolean.TRUE);
    return params;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Question> getSomeQuizQuestionsWithGold(String quizID, int amount) {
    PersistenceManager pm = getPersistenceManager();

    try {
      Quiz quiz = quizRepository.singleGetObjectById(Quiz.generateKeyFromID(quizID));
      int questionsWithGold = quiz.getGold();
      Query query = getQuizGoldQuestionsQuery(pm, quizID);
      setRandomRange(query, questionsWithGold, amount);
      ArrayList<Question> result = new ArrayList<Question>(
          (List<Question>) query
              .executeWithMap(getQuizGoldQuestionsParameters(quizID)));
      return removeInvalidQuestions(result);
    } finally {
      pm.close();
    }
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Question> getSomeQuizQuestionsWithSilver(String quizID, int amount) {
    PersistenceManager pm = getPersistenceManager();

    try {
      Quiz quiz = quizRepository.get(quizID);
      int questionsWithSilver = quiz.getQuestions() - quiz.getGold();
      Query query = getQuizSilverQuestionsQuery(pm, quizID);
      setRandomRange(query, questionsWithSilver, amount);
      ArrayList<Question> result = new ArrayList<Question>(
          (List<Question>) query
              .executeWithMap(getQuizSilverQuestionsParameters(quizID)));

      return removeInvalidQuestions(result);
    } finally {
      pm.close();
    }
  }

  // Removes invalid questions that have answers' kind = null in the given questions list
  // and returns the remaining questions.
  // TODO(chunhowt): This is temporary fix to resolve "corruption" in datastore where the
  // answers have null values in all the field, and it is yet unclear whether it is due
  // to errors during datastore insertion or during updates.
  private ArrayList<Question> removeInvalidQuestions(final ArrayList<Question> questions) {
    ArrayList<Question> newQuestions = new ArrayList<Question>();
    for (final Question question : questions) {
      boolean isValidQuestion = true;
      for (final Answer answer : question.getAnswers()) {
        if (answer.getKind() == null) {
          isValidQuestion = false;
          break;
        }
      }
      if (isValidQuestion) {
        newQuestions.add(question);
      }
    }
    return newQuestions;
  }

  public void setRandomRange(Query query, int size, int amount) {
    int lower = (int) (Math.random() * Math.max(0, size - amount));
    int upper = Math.min(size, lower + amount);
    query.setRange(lower, upper); // upper is excluded index
  }

  public ArrayList<Question> getQuizQuestionsWithGold(String quizID) {
    String key = MemcacheKey.getGoldQuizQuestionsByQuiz(quizID);
    String filter = "quizID == quizParam && hasGoldAnswer == hasGoldParam";
    String declaredParameters = "String quizParam, Boolean hasGoldParam";
    return getQuestionsWithCaching(key, filter, declaredParameters,
        getQuizGoldQuestionsParameters(quizID));
  }

  @SuppressWarnings("unchecked")
  public List<Question> getQuizQuestionsByKeys(List<Key> keys) {
    Query q = getPersistenceManager().newQuery(
        "select from " + Question.class.getName() + " where key == :keys");
      return (List<Question>) q.execute(keys);
  }

  public void storeQuizQuestion(Question q) {
    singleMakePersistent(q);
  }

  public void removeWithoutUpdates(Long questionID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Question qq = pm.getObjectById(Question.class, questionID);
      pm.deletePersistent(qq);
    } finally {
      pm.close();
    }
  }

  public List<UserAnswer> getUserAnswers(Question question) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("questionID == questionParam");
      q.declareParameters("Long questionParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionParam", question.getID());

      @SuppressWarnings("unchecked")
      List<UserAnswer> result = (List<UserAnswer>) q.executeWithMap(params);
      return result;
    } finally {
      pm.close();
    }
  }

  public int getNumberOfUserAnswersExcludingIDK(String questionID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("questionID == questionIDParam && action == submitParam");
      q.declareParameters("String questionIDParam, String submitParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", questionID);
      params.put("submitParam", "Submit");

      @SuppressWarnings("unchecked")
      List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
      return results.size();
    } finally {
      pm.close();
    }
  }

  public int getNumberOfCorrectUserAnswers(String questionID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("questionID == questionIDParam && action == submitParam && " +
                  "isCorrect == correctParam");
      q.declareParameters("String questionIDParam, String submitParam, Boolean correctParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", questionID);
      params.put("submitParam", "Submit");
      params.put("correctParam", Boolean.TRUE);

      @SuppressWarnings("unchecked")
      List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
      return results.size();
    } finally {
      pm.close();
    }
  }
}
