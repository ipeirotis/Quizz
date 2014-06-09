package us.quizz.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

import us.quizz.entities.QuizPerformance;
import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.SurvivalProbabilityResultRepository;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SurvivalProbabilityService extends OfyBaseService<SurvivalProbabilityResult> {
  private static final Logger logger = Logger.getLogger(SurvivalProbabilityService.class.getName());

  // Number of minutes to cache survival probability in Memcache.
  private static final int SURVIVAL_PROBABILITIES_CACHED_TIME_MINS = 24 * 60;  // 24 hours.

  private QuizPerformanceService quizPerformanceService;
  private Cache<String, Map<Integer, Map<Integer, Integer>>> inMemoryCache;

  @Inject
  public SurvivalProbabilityService(
      QuizPerformanceService quizPerformanceService,
      SurvivalProbabilityResultRepository survivalProbabilityResultRepository) {
    super(survivalProbabilityResultRepository);
    this.quizPerformanceService = quizPerformanceService;
    this.inMemoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(SURVIVAL_PROBABILITIES_CACHED_TIME_MINS, TimeUnit.MINUTES).build();
  }

  // Constructs a new SurvivalProbabilityResult for the stats given.
  // This first looks into the caches for the survival probability count for different
  // values of numCorrect and numIncorrect.
  // If it is not found, we return the default survival probability.
  // Else, if it is found, we compute a new surival probability results based on the stats.
  public SurvivalProbabilityResult getSurvivalProbability(
      String quizID, Integer fromNumCorrect, Integer toNumCorrect,
      Integer fromNumIncorrect, Integer toNumIncorrect,
      Integer fromNumExploit, Integer toNumExploit) {
    Map<Integer, Map<Integer, Integer>> values = getCachedValues(quizID);
    // A default survival probability.
    SurvivalProbabilityResult defaultResult = SurvivalProbabilityResult.getDefaultResult(
        fromNumCorrect, fromNumIncorrect, fromNumExploit,
        toNumCorrect, toNumIncorrect, toNumExploit);
    if (values == null) {  // empty cache
      return defaultResult;
    }

    Integer usersFrom = values.containsKey(fromNumCorrect) ?
        values.get(fromNumCorrect).get(fromNumIncorrect) : null;
    Integer usersTo = values.containsKey(toNumCorrect) ?
        values.get(toNumCorrect).get(toNumIncorrect) : null;
    if (usersFrom == null || usersTo == null || usersFrom == 0) { 
      return defaultResult;
    }

    // Probability of survival is just how many users have the "to" stats vs "from" stats.
    double psurvival = 1.0 * usersTo / usersFrom;
    return new SurvivalProbabilityResult(
        fromNumCorrect, fromNumIncorrect, fromNumExploit,
        toNumCorrect, toNumIncorrect, toNumExploit,
        usersFrom, usersTo, psurvival, false);
  }

  // Returns a list of SurvivalProbabilityResult for the quizID given from the cache values.
  public List<SurvivalProbabilityResult> getSurvivalProbabilities(String quizID) {
    List<SurvivalProbabilityResult> result = new ArrayList<SurvivalProbabilityResult>();
    Map<Integer, Map<Integer, Integer>> values = getCachedValues(quizID);
    if (values == null) {
      return result;
    }
    return valuesToResults(quizID, values);
  }

  // Converts from survival probability counts to list of SurvivalProbabilityResult.
  // values: map from numCorrect -> (numIncorrect -> count).
  private List<SurvivalProbabilityResult> valuesToResults(
      String quizID, Map<Integer, Map<Integer, Integer>> values){
    List<SurvivalProbabilityResult> result = new ArrayList<SurvivalProbabilityResult>();
    int numCorrectMax = 0;
    int numIncorrectMax = 0;
    for (int numCorrect : values.keySet()) {
      if (numCorrect > numCorrectMax) {
        numCorrectMax = numCorrect;
      }
      Map<Integer, Integer> numIncorrects = values.get(numCorrect);
      for (int numIncorrect : numIncorrects.keySet()) {
        if (numIncorrect > numIncorrectMax) {
          numIncorrectMax = numIncorrect;
        }
      }
    }

    // TODO: incorporate numExploits in the loop
    int numExploits = 0;
    // For each possible numCorrect and numIncorrect, we try to compute the survival probability
    // of the user if he gets another question correct/incorrect.
    for (int numCorrect = 0; numCorrect <= numCorrectMax; ++numCorrect) {
      for (int numIncorrect = 0; numIncorrect <= numIncorrectMax; ++numIncorrect) {
        SurvivalProbabilityResult r1 = getSurvivalProbability(
            quizID, numCorrect, numCorrect + 1,
            numIncorrect, numIncorrect, numExploits, numExploits);
        // However, we ignore those where we don't have stats (default value).
        if (!r1.getIsDefault()) {
          result.add(r1);
        }
        SurvivalProbabilityResult r2 = getSurvivalProbability(
            quizID, numCorrect, numCorrect,
            numIncorrect, numIncorrect + 1, numExploits, numExploits);
        // Ignore those where we don't have stats (default value).
        if (!r2.getIsDefault()) {
          result.add(r2);
        }
      }
    }
    return result;
  }

  private void PutMapValue(Map<Integer, Map<Integer, Integer>> values,
      Integer numCorrect, Integer numIncorrect, Integer numUsers) {
    if (values.get(numCorrect) == null) {
      values.put(numCorrect, new HashMap<Integer, Integer>());
    }
    if (values.get(numCorrect).get(numIncorrect) == null) {
      Map<Integer, Integer> countMap = values.get(numCorrect);
      countMap.put(numIncorrect, numUsers);
      values.put(numCorrect, countMap);
    } else if (values.get(numCorrect).get(numIncorrect) != numUsers) {
      logger.warning("survival probability count doesn't match for numCorrect: " + numCorrect +
          " and numIncorrect: " + numIncorrect + ". numUsers is " + numUsers +
          " but already has " + values.get(numCorrect).get(numIncorrect));
    }
  }

  // Converts from list of SurvivalProbabilityResult to survival probability counts.
  protected Map<Integer, Map<Integer, Integer>> resultsToMap(
      List<SurvivalProbabilityResult> results) {
    Map<Integer, Map<Integer, Integer>> values = new HashMap<Integer, Map<Integer, Integer>>();
    Integer correctMax = 0;
    Integer incorrectMax = 0;
    for (SurvivalProbabilityResult result : results) {
      PutMapValue(
          values, result.getCorrectFrom(), result.getIncorrectFrom(), result.getUsersFrom());
      PutMapValue(
          values, result.getCorrectTo(), result.getIncorrectTo(), result.getUsersTo());
    }
    return values;
  }

  // Returns the survival probability counts, first from inmemory cache, or then from memcache.
  // Returns a map of # correct -> (# incorrect -> count) for the users who did the given quizID.
  @SuppressWarnings("unchecked")
  private Map<Integer, Map<Integer, Integer>> getCachedValues(String quizId) {
    String key = MemcacheKey.getSurvivalProbabilities(quizId);
    // Try in memory cache.
    Map<Integer, Map<Integer, Integer>> result = inMemoryCache.getIfPresent(key);
    if (result == null) {
      // Try memcache.
      result = CachePMF.get(key, HashMap.class);
      if (result == null) {
        // Try datastore.
        result = resultsToMap(listAll());
        if (result != null) {
          // save to memcache
          CachePMF.put(key, result, SURVIVAL_PROBABILITIES_CACHED_TIME_MINS * 60);
        }
      }
      // save to in memory cache.
      if (result != null) {
        inMemoryCache.put(key, result);
      }
    }
    return result;
  }

  // Caches all the survival probability counts into the memcache.
  public void cacheValuesInMemcache(String quizId) {
    Map<Integer, Map<Integer, Integer>> values = getCountsForSurvivalProbability(quizId);
    String key = MemcacheKey.getSurvivalProbabilities(quizId);
    CachePMF.put(key, values, SURVIVAL_PROBABILITIES_CACHED_TIME_MINS * 60);
  }

  // Stores all the survival probability counts from memcache to list of survival probability
  // results in datastore.
  @SuppressWarnings("unchecked")
  public void saveValuesInDatastore(String quizId) {
    String key = MemcacheKey.getSurvivalProbabilities(quizId);
    Map<Integer, Map<Integer, Integer>> values = CachePMF.get(key, HashMap.class);
    if (values == null) {
      return;
    }
    saveAll(valuesToResults(quizId, values));
  }
  
  // Calculates the number of users that have at least "a" correct answers and "b" incorrect
  // answers for a given quiz (or calculate the stats across all quizzes if quizID == null).
  // Returns a map from (# correct -> (# incorrect -> count)).
  public Map<Integer, Map<Integer, Integer>> getCountsForSurvivalProbability(String quizID) {
    List<QuizPerformance> list = quizPerformanceService.getQuizPerformancesByQuiz(quizID);
    // # correct -> (# incorrect -> count)
    Map<Integer, Map<Integer, Integer>> result = new HashMap<Integer, Map<Integer, Integer>>();

    for (QuizPerformance quizPerformance : list) {
      Integer correct = quizPerformance.getCorrectanswers();
      Integer incorrect = quizPerformance.getIncorrectanswers();
      if (correct == null || incorrect == null) continue;
      increaseCounts(result, correct, incorrect);
    }
    return result;
  }

  // Increments the count in result for all pairs of [0, correct] and [0, incorrect].
  // Params:
  //   result: (# correct -> (# incorrect -> count)).
  private void increaseCounts(Map<Integer, Map<Integer, Integer>> result,
      Integer correct, Integer incorrect) {
    for (int a = 0; a <= correct; ++a)  {
      Map<Integer, Integer> cntA = result.get(a);
      if (cntA == null) {
        cntA = new HashMap<Integer, Integer>();
        result.put(a, cntA);
      }

      for (int b = 0; b <= incorrect; ++b)  {
        Integer cntAB = cntA.get(b);
        if (cntAB == null) {
          cntAB = 0;
        }
        cntA.put(b, cntAB + 1);
      }
      result.put(a, cntA);
    }
  }

}
