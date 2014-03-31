package us.quizz.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

import us.quizz.entities.SurvivalProbabilityResult;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SurvivalProbabilityService {
  // Number of minutes to cache survival probability in Memcache.
  private static final int SURVIVAL_PROBABILITIES_CACHED_TIME_MINS = 24 * 60;  // 24 hours.

  private QuizPerformanceRepository quizPerformanceRepository;
  private Cache<String, Map<Integer, Map<Integer, Integer>>> inMemoryCache;

  @Inject
  public SurvivalProbabilityService(
      QuizPerformanceRepository quizPerformanceRepository) {
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.inMemoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(SURVIVAL_PROBABILITIES_CACHED_TIME_MINS, TimeUnit.MINUTES).build();
  }

  public SurvivalProbabilityResult getSurvivalProbability(String quizID, Integer a_from,
      Integer a_to, Integer b_from, Integer b_to, Integer c_from, Integer c_to) {
    Map<Integer, Map<Integer, Integer>> values = getCachedValues(quizID);

    // We assume a default survival probability
    SurvivalProbabilityResult defaultResult = SurvivalProbabilityResult.getDefaultResult(a_from, b_from, c_from, a_to, b_to, c_to);
    
    if (values == null) {//empty cache
      return defaultResult; 
    }
    
    Integer users_from = values.containsKey(a_from) ? values.get(a_from).get(b_from) : null;
    Integer users_to = values.containsKey(a_to) ? values.get(a_to).get(b_to) : null;
    
    if (users_from == null || users_to == null || users_from == 0) { 
      return defaultResult;
    }
    
    double psurvival = 1.0 * users_to / users_from;

    return new SurvivalProbabilityResult(a_from, b_from, c_from, a_to, b_to, c_to, users_from, users_to, psurvival, false);
  }

  public List<SurvivalProbabilityResult> getSurvivalProbabilities(String quizID) {
    List<SurvivalProbabilityResult> result = new ArrayList<SurvivalProbabilityResult>();

    Map<Integer, Map<Integer, Integer>> values = getCachedValues(quizID);
    if (values == null) return result;

    int aMax = 0;
    int bMax = 0;
    for (int a : values.keySet()) {
      if (a > aMax) aMax = a;
      Map<Integer, Integer> bs = values.get(a);
      for (int b : bs.keySet()) {
        if (b > bMax) bMax = b;
      }
    }

    //TODO: incorporate c (number of exploits) in the loop
    int c=0;
    for (int a = 0; a < aMax; a++) {
      for (int b = 0; b < bMax; b++) {
        SurvivalProbabilityResult r1 = getSurvivalProbability(quizID, a, a + 1,  b, b, c, c);
        if (!r1.getIsDefault()) result.add(r1);
        SurvivalProbabilityResult r2 = getSurvivalProbability(quizID, a, a, b, b + 1, c, c);
        if (!r2.getIsDefault()) result.add(r2);
      }
    }

    return result;
  }
  
  @SuppressWarnings("unchecked")
  private Map<Integer, Map<Integer, Integer>> getCachedValues(String quizId) {
    String key = MemcacheKey.getSurvivalProbabilities(quizId);
    Map<Integer, Map<Integer, Integer>> result = inMemoryCache.getIfPresent(key);

    if (result == null) {
      result = CachePMF.get(key, HashMap.class);
      if (result != null) {
        inMemoryCache.put(key, result);
      }
    }
    return result;
  }
  
  public void cacheValuesInMemcache(String quizId) {
    Map<Integer, Map<Integer, Integer>> values =
        quizPerformanceRepository.getCountsForSurvivalProbability(quizId);
    String key = MemcacheKey.getSurvivalProbabilities(quizId);
    CachePMF.put(key, values, SURVIVAL_PROBABILITIES_CACHED_TIME_MINS * 60);
  }


}
