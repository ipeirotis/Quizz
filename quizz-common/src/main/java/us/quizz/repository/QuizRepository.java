package us.quizz.repository;

import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

import us.quizz.entities.Answer;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.entities.QuizPerformance;
import us.quizz.entities.UserAnswer;
import us.quizz.utils.CachePMF;
import us.quizz.utils.PMF;

import java.util.ArrayList;
import java.util.List;

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
  
  public Quiz get(String id){
    return singleGetObjectById(Quiz.generateKeyFromID(id));
  }

  protected <T> void deleteAll(PersistenceManager pm, String quizID,
      Class<T> itemsClass) {
    Query q = pm.newQuery(itemsClass);
    q.setFilter("quizID == quizIDParam");
    q.declareParameters("String quizIDParam");
    List<?> items = (List<?>) q.execute(quizID);
    pm.deletePersistentAll(items);
  }

  public void deleteQuiz(String quizID) {
    PersistenceManager pm = PMF.getPM();
    try {
      Quiz quiz = pm.getObjectById(Quiz.class, Quiz.generateKeyFromID(quizID));
      pm.deletePersistent(quiz);

      Class<?>[] itemsClasses = new Class<?>[] { UserAnswer.class,
          Answer.class, Question.class };
      for (Class<?> cls : itemsClasses) {
        deleteAll(pm, quizID, cls);
      }
    } finally {
      pm.close();
    }
  }

  protected <T> Integer getNumberOf(String keyPrefix,
      boolean useCache, String quiz, Class<T> queryClass) {
    String key = keyPrefix + "_" + quiz;

    if (useCache) {
      Integer result = CachePMF.get(key, Integer.class);
      if (result != null)
        return result;
    }

    PersistenceManager pm = PMF.getPM();
    try {
      Query q = pm.newQuery(queryClass);
      q.setFilter("quizID == quizParam");
      q.declareParameters("String quizParam");
      q.getFetchPlan().setFetchSize(1000);
      q.setResult("quizID");
      List<?> results = (List<?>) q.execute(quiz);
      Integer result = results.size();
      CachePMF.put(key, result);
      return result;
    } finally {
      pm.close();
    }
  }

  public Integer getNumberOfGoldQuestions(String quizID,
      boolean useCache) {
    String key = "goldQuestions_" + quizID;

    if (useCache) {
      Integer result = CachePMF.get(key, Integer.class);
      if (result != null)
        return result;
    }

    PersistenceManager pm = PMF.getPM();
    try {
      Query query = pm.newQuery(Question.class);
      query.setFilter("quizID == quizParam && hasGoldAnswer==hasGoldParam");
      query.declareParameters("String quizParam, Boolean hasGoldParam");
      query.getFetchPlan().setFetchSize(1000);

      List<?> results = (List<?>) query.execute(quizID, Boolean.TRUE);
      Integer result = results.size();
      CachePMF.put(key, result);
      return result;
    } finally {
      pm.close();
    }
  }

  public Integer getNumberOfQuizQuestions(String quiz, boolean usecache) {
    return getNumberOf("numquizquestions", usecache, quiz, Question.class);
  }

  public Integer getNumberOfUserAnswers(String quiz, boolean usecache) {
    return getNumberOf("quizuseranswers", usecache, quiz, UserAnswer.class);
  }

  @SuppressWarnings("unchecked")
  public List<Quiz> getQuizzes() {

    String key = "list_quizzes";
    List<Quiz> quizlist = CachePMF.get(key, List.class);
    if (quizlist != null)
      return quizlist;

    PersistenceManager pm = PMF.getPM();
    Query query = pm.newQuery(Quiz.class);
    quizlist = (List<Quiz>) query.execute();
    pm.close();
    List<Quiz> list = new ArrayList<Quiz>(quizlist);
    CachePMF.put(key, list);
    return quizlist;
  }

  public void updateQuizCounts(String quiz) {
    Quiz q = singleGetObjectById(Quiz.generateKeyFromID(quiz));

    Integer count = getNumberOfQuizQuestions(quiz, false);
    q.setQuestions(count);

    count = getNumberOfUserAnswers(quiz, false);
    q.setSubmitted(count);

    count = getNumberOfGoldQuestions(quiz, false);
    q.setGold(count);

    count = userReferralRepository.getUserIDsByQuiz(q.getQuizID()).size();
    q.setTotalUsers(count + 1); // +1 for smoothing, ensuring no division by 0

    List<QuizPerformance> perf = quizPerformanceRepository
        .getQuizPerformancesByQuiz(q.getQuizID());

    int contributingUsers = perf.size();
    q.setContributingUsers(contributingUsers + 1); // +1 for smoothing,
                            // ensuring no division by 0
    q.setConversionRate(1.0 * q.getContributingUsers() / q.getTotalUsers());

    int totalCorrect = 1; // +1 for smoothing, ensuring no division by 0
    int totalAnswers = 1; // +1 for smoothing, ensuring no division by 0
    double bits = 0;
    double avgCorrectness = 0;

    for (QuizPerformance qp : perf) {
      totalCorrect += qp.getCorrectanswers();
      totalAnswers += qp.getTotalanswers();
      avgCorrectness += qp.getPercentageCorrect();
      bits += qp.getScore();
    }
    q.setCorrectAnswers(totalCorrect);
    q.setTotalAnswers(totalAnswers);

    q.setCapacity(bits / q.getContributingUsers());

    q.setAvgUserCorrectness(avgCorrectness / q.getContributingUsers());
    q.setAvgAnswerCorrectness(1.0 * q.getCorrectAnswers() / q.getTotalAnswers());

    singleMakePersistent(q);
  }
}
