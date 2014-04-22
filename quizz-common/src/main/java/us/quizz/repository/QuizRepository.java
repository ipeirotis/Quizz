package us.quizz.repository;

import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class QuizRepository extends BaseRepository<Quiz>{
  private UserReferralRepository userReferralRepository;
  private QuizPerformanceRepository quizPerformanceRepository;

  @Inject
  public QuizRepository(UserReferralRepository userReferralRepository,
      QuizPerformanceRepository quizPerformanceRepository) {
    super(Quiz.class);
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.userReferralRepository = userReferralRepository;
  }
  
  @Override
  protected Key getKey(Quiz item) {
    return item.getKey();
  }

  public Quiz get(String quizID){
    return singleGetObjectById(Quiz.generateKeyFromID(quizID));
  }

  private <T> void deleteAll(PersistenceManager pm, String quizID, Class<T> itemsClass) {
    Query q = pm.newQuery(itemsClass);
    q.setFilter("quizID == quizIDParam");
    q.declareParameters("String quizIDParam");

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("quizIDParam", quizID);

    pm.deletePersistentAll(fetchAllResults(q, params));
  }

  // Deletes the quizID given along with all UserAnswer, Answer and Question associated with the
  // quizID.
  public void deleteQuiz(String quizID) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Quiz quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(quizID));
      pm.deletePersistent(quiz);

      Class<?>[] itemsClasses = new Class<?>[] { UserAnswer.class, Answer.class, Question.class };
      for (Class<?> cls : itemsClasses) {
        deleteAll(pm, quizID, cls);
      }
    } finally {
      pm.close();
    }
  }

  private <T> Integer getNumberOf(
      String key, boolean useCache, String quizID, Class<T> queryClass) {
    if (useCache) {
      Integer result = CachePMF.get(key, Integer.class);
      if (result != null)
        return result;
    }

    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(queryClass);
      q.setFilter("quizID == quizParam");
      q.declareParameters("String quizParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quizID);

      Integer count = countResults(q, params);
      CachePMF.put(key, count);
      return count;
    } finally {
      pm.close();
    }
  }

  public Integer getNumberOfGoldQuestions(String quizID, boolean useCache) {
    String key = MemcacheKey.getNumGoldQuestions(quizID);
    if (useCache) {
      Integer result = CachePMF.get(key, Integer.class);
      if (result != null)
        return result;
    }

    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = pm.newQuery(Question.class);
      query.setFilter("quizID == quizParam && hasGoldAnswer == hasGoldParam");
      query.declareParameters("String quizParam, Boolean hasGoldParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quizID); 
      params.put("hasGoldParam", Boolean.TRUE);

      Integer count = countResults(query, params);
      CachePMF.put(key, count);
      return count;
    } finally {
      pm.close();
    }
  }

  public Integer getNumberOfQuizQuestions(String quiz, boolean usecache) {
    return getNumberOf(MemcacheKey.getNumQuizQuestions(quiz), usecache, quiz, Question.class);
  }

  public Integer getNumberOfUserAnswers(String quiz, boolean usecache) {
    return getNumberOf(MemcacheKey.getNumUserAnswers(quiz), usecache, quiz, UserAnswer.class);
  }

  @SuppressWarnings("unchecked")
  public List<Quiz> getQuizzes() {
    String key = MemcacheKey.getQuizzesList();
    List<Quiz> quizlist = CachePMF.get(key, List.class);
    if (quizlist != null)
      return quizlist;

    PersistenceManager pm = getPersistenceManager();
    try {
      Query query = pm.newQuery(Quiz.class);
      quizlist = (List<Quiz>) fetchAllResults(query, new HashMap<String, Object>());
    } finally {
      pm.close();
    }
    CachePMF.put(key, quizlist);
    return quizlist;
  }

  public Quiz updateQuizCounts(String quizID) {
    Quiz quiz = get(quizID);
    Integer count = getNumberOfQuizQuestions(quizID, false);
    quiz.setQuestions(count);

    count = getNumberOfUserAnswers(quizID, false);
    quiz.setSubmitted(count);

    count = getNumberOfGoldQuestions(quizID, false);
    quiz.setGold(count);

    // TODO(chunhowt): UserReferral is broken now, so this will always return 0.
    count = userReferralRepository.getUserIDsByQuiz(quizID).size();
    quiz.setTotalUsers(count + 1);  // +1 for smoothing, ensuring no division by 0

    List<QuizPerformance> perf = quizPerformanceRepository
        .getQuizPerformancesByQuiz(quizID);

    int contributingUsers = perf.size();
    // +1 for smoothing, ensuring no division by 0
    quiz.setContributingUsers(contributingUsers + 1);
    quiz.setConversionRate(1.0 * quiz.getContributingUsers() / quiz.getTotalUsers());
 
    // +1 for smoothing, ensuring no division by 0
    int totalCorrect = 1;
    int totalAnswers = 1;
    int totalCalibrationAnswers = 1;
    double bits = 0;
    double avgCorrectness = 0;

    for (QuizPerformance qp : perf) {
      totalCorrect += qp.getCorrectanswers();
      totalAnswers += qp.getTotalanswers();
      totalCalibrationAnswers += qp.getTotalCalibrationAnswers();
      avgCorrectness += qp.getPercentageCorrect();
      bits += qp.getScore();
    }
    quiz.setCorrectAnswers(totalCorrect);
    quiz.setTotalAnswers(totalAnswers);
    quiz.setTotalCalibrationAnswers(totalCalibrationAnswers);
    quiz.setTotalCollectionAnswers(totalAnswers - totalCalibrationAnswers);
    quiz.setCapacity(bits / quiz.getContributingUsers());
    quiz.setAvgUserCorrectness(avgCorrectness / quiz.getContributingUsers());
    quiz.setAvgAnswerCorrectness(
        1.0 * quiz.getCorrectAnswers() / quiz.getTotalCalibrationAnswers());
    return singleMakePersistent(quiz);
  }
}
