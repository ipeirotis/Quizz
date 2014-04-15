package us.quizz.repository;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class QuizQuestionRepository extends BaseRepository<Question> {
  // Multiplier used to fetch more questions than being asked when choosing the next questions
  // to allow us to fetch enough candidate questions for each user.
  private static final int QUESTION_FETCHING_MULTIPLIER = 3;

  // Number of seconds to cache the quiz questions.
  private static final int QUESTIONS_CACHED_TIME_SECONDS = 5 * 60;  // 5 minutes.

  // Default maximum questions to be fetched from the datastore.
  private static final int DEFAULT_QUESTIONS_MAX_FETCH_SIZE = 1000;

  private static final Logger logger = Logger.getLogger(QuizQuestionRepository.class.getName());

  QuizRepository quizRepository;
  UserAnswerRepository userAnswerRepository;

  @Inject
  public QuizQuestionRepository(QuizRepository quizRepository,
      UserAnswerRepository userAnswerRepository) {
    super(Question.class);
    this.quizRepository = quizRepository;
    this.userAnswerRepository = userAnswerRepository;
  }

  @Override
  protected Key getKey(Question item) {
    return item.getKey();
  }

  // Returns the next n calibration and collection questions in the given quizID for a given userID.
  // The map result will has two values, mapping from:
  // - "calibration" -> set of calibration questions.
  // - "collection" -> set of collection questions.
  // TODO(chunhowt): Const the keys in server and in angularJs.
  public Map<String, Set<Question>> getNextQuizQuestions(String quizID, int n, String userID) {
    // First, we try to get a list of questions that the user has answered before for this quiz.
    List<UserAnswer> userAnswers = userAnswerRepository.getUserAnswers(quizID, userID);
    Set<Long> questionIDs = new HashSet<Long>();
    for (UserAnswer userAnswer : userAnswers) {
      questionIDs.add(userAnswer.getQuestionID());
    }

    // Then, we pick calibration and collection questions from datastore and filter the questions
    // previously asked from the results.
    Map<String, Set<Question>> result = new HashMap<String, Set<Question>>();
    List<Question> goldQuestions = getSomeQuizQuestionsWithGold(quizID, n, questionIDs);
    result.put("calibration", new HashSet<Question>(goldQuestions));

    List<Question> silverQuestions = getSomeQuizQuestionsWithSilver(quizID, n, questionIDs);
    result.put("collection", new HashSet<Question>(silverQuestions));
    return result;
  }

  public Question getQuizQuestion(String questionID) {
    return getQuizQuestion(Long.parseLong(questionID));
  }

  // Returns the Question with the given questionID as the key if found, else returns null if
  // there is a datastore error when fetching the question.
  public Question getQuizQuestion(Long questionID) {
    Question question = get(questionID, true  /* use transaction */);

    // Sanity check on the datastore fetch results to make sure all embedded entities are fetched.
    if (question == null) {
      logger.log(Level.SEVERE, "getQuizQuestion: " + questionID + ": transaction fail. " +
          "This is likely a transient error with datastore.");
      return null;
    }

    if (question.getAnswers() == null) {
      logger.log(Level.SEVERE, "getQuizQuestion: " + questionID + ": answers array is null. " +
          "This is likely a transient error with datastore.");
      return null;
    }

    for (Answer answer : question.getAnswers()) {
      if (answer.getKind() == null) {
        logger.log(Level.SEVERE, "getQuizQuestion: " + questionID + ": one answer is null. " +
            "This is likely a transient error with datastore.");
        return null;
      }
    }

    return question;
  }

  private Query getQuestionQuery(
      PersistenceManager pm, String filter, String declaredParameters) {
    Query query = pm.newQuery(Question.class);
    if (!filter.isEmpty() && !declaredParameters.isEmpty()) {
      query.setFilter(filter);
      query.declareParameters(declaredParameters);
    }
    return query;
  }

  private List<Question> getAllQuizQuestions(Query query, Map<String, Object> params) {
    query.setRange(0, DEFAULT_QUESTIONS_MAX_FETCH_SIZE);
    Cursor cursor = null;
    List<Question> questions = new ArrayList<Question>();
    while (true) {
      if (cursor != null) {
        Map<String, Object> extensionMap = new HashMap<String, Object>();
        extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
        query.setExtensions(extensionMap);
      }

      @SuppressWarnings("unchecked")
      List<Question> results = (List<Question>) query.executeWithMap(params);
      if (results.size() == 0) {
        break;
      }
      for (Question q : results) {
        if (q.getAnswers() != null) {
          for (Answer a : q.getAnswers()) {
            a.getID();
          }
          questions.add(q);
        }
      }
      cursor = JDOCursorHelper.getCursor(results);
    }

    return removeInvalidQuestions(questions);
  }

  public List<Question> getQuizQuestions() {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = getQuestionQuery(pm, "", "");
      return getAllQuizQuestions(query, new HashMap<String, Object>());
    } finally {
      pm.close();
    }
  }

  private List<Question> getQuestions(
      String filter, String declaredParameters, Map<String, Object> params) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = getQuestionQuery(pm, filter, declaredParameters);
      return getAllQuizQuestions(query, params);
    } finally {
      pm.close();
    }
  }

  private List<Question> getQuestionsWithCaching(
      String key, String filter, String declaredParameters, Map<String, Object> params) {
    @SuppressWarnings("unchecked")
    List<Question> questions = (List<Question>) CachePMF.get(key, ArrayList.class);
    if (questions != null) {
      return questions;
    }
    questions = getQuestions(filter, declaredParameters, params);
    CachePMF.put(key, questions);
    return questions;
  }

  public List<Question> getQuizQuestions(String quizid) {
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

  // Returns a list of calibration questions that is at least size min_amount
  // if feasible (eg. enough candidate questions).
  @SuppressWarnings("unchecked")
  private List<Question> getSomeQuizQuestionsWithGold(
      String quizID, int min_amount, Set<Long> questionIDs) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = getQuizGoldQuestionsQuery(pm, quizID);
      query.setOrdering("totalUserScore ascending");
      // Fetch more than needed as there are filtering done below, proportional to how
      // many questions users have answered.
      query.setRange(0, min_amount + questionIDs.size() * QUESTION_FETCHING_MULTIPLIER);

      List<Question> result =
          (List<Question>) query.executeWithMap(getQuizGoldQuestionsParameters(quizID));
      result = removeInvalidQuestions(result);
      return diversifyQuestionsAsked(result, questionIDs, min_amount,
          false  /* don't repeat if not enough questions. */);
    } finally {
      pm.close();
    }
  }

  // Returns a list of collection questions that is at least size min_amount
  // if feasible (eg. enough candidate questions).
  @SuppressWarnings("unchecked")
  private List<Question> getSomeQuizQuestionsWithSilver(
      String quizID, int min_amount, Set<Long> questionIDs) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = getQuizSilverQuestionsQuery(pm, quizID);
      query.setOrdering("totalUserScore ascending");
      // Fetch more than needed as there are filtering done below, proportional to how
      // many questions users have answered.
      query.setRange(0, min_amount + questionIDs.size() * QUESTION_FETCHING_MULTIPLIER);

      List<Question> result =
          (List<Question>) query.executeWithMap(getQuizSilverQuestionsParameters(quizID));
      result = removeInvalidQuestions(result);
      return diversifyQuestionsAsked(result, questionIDs, min_amount,
          true  /* repeat if not enough questions. */);
    } finally {
      pm.close();
    }
  }

  // Filters questions to prefer choosing n questions that are not contained in the set.
  // If repeatQuestions is true, allows duplicate questions when there is not enough candidate
  // questions that have never been answered before.
  // TODO(chunhowt): This is essentially an attempt to do a join query of Question and
  // UserAnswer entities but it seems like datastore doesn't allow such a join query. This
  // works for now, but there should be a better solution.
  private List<Question> diversifyQuestionsAsked(
      List<Question> questions, Set<Long> questionIDs, int n, boolean repeatQuestions) {
    List<Question> results = new ArrayList<Question>();
    List<Question> filtered = new ArrayList<Question>();
    for (Question question : questions) {
      // Question not in questionIDs (not asked before) is selected first.
      if (!questionIDs.contains(question.getID())) {
        results.add(question);
        if (results.size() == n) {
          break;
        }
      } else {
        filtered.add(question);
      }
    }

    // However, some quizz might be too small and a user has answered all its questions, here
    // we will just randomly choose some questions users answered before.
    // TODO(chunhowt): Instead of doing this, we can send a message to user to redirect him
    // to another quizz.
    if (results.size() < n && repeatQuestions) {
      int numToChoose = Math.min(n - results.size(), filtered.size());
      results.addAll(filtered.subList(0, numToChoose));
    }
    return results;
  }

  // Removes invalid questions that have answers' kind = null in the given questions list
  // and returns the remaining questions.
  // TODO(chunhowt): Datastore sometimes has transient error and returns Questions with embedded
  // Answers entities not fully fetched. We should try to figure out how to do robust datastore
  // query and remove this.
  private List<Question> removeInvalidQuestions(List<Question> questions) {
    List<Question> newQuestions = new ArrayList<Question>();
    for (Question question : questions) {
      boolean isValidQuestion = true;
      if (question.getAnswers() == null) {
        logger.log(Level.WARNING, "removeInvalidQuestions: " + question.getKey().getId() +
            " has null answers. This is likely permanent error due to answers field " +
            "accidentally set to null.");
        continue;
      }
      for (Answer answer : question.getAnswers()) {
        if (answer.getKind() == null) {
          isValidQuestion = false;
          break;
        }
      }
      if (isValidQuestion) {
        newQuestions.add(question);
      } else {
        logger.log(Level.WARNING, "removeInvalidQuestions: " + question.getKey().getId() +
            ". This is likely transient error due to datastore fetching error.");
      }
    }
    return newQuestions;
  }

  public List<Question> getQuizQuestionsWithGold(String quizID) {
    String key = MemcacheKey.getGoldQuizQuestionsByQuiz(quizID);
    String filter = "quizID == quizParam && hasGoldAnswer == hasGoldParam";
    String declaredParameters = "String quizParam, Boolean hasGoldParam";
    return getQuestionsWithCaching(key, filter, declaredParameters,
        getQuizGoldQuestionsParameters(quizID));
  }

  @SuppressWarnings("unchecked")
  public List<Question> getQuizQuestionsByKeys(List<Key> keys) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(
          "select from " + Question.class.getName() + " where key == :keys");
      List<Question> results = (List<Question>) q.execute(keys);
      return removeInvalidQuestions(results);
    } finally {
      pm.close();
    }
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
      q.declareParameters("Long questionIDParam, String submitParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", Long.parseLong(questionID));
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
      q.declareParameters("Long questionIDParam, String submitParam, Boolean correctParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("questionIDParam", Long.parseLong(questionID));
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
