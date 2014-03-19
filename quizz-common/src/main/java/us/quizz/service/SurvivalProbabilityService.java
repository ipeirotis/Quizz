package us.quizz.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.utils.CachePMF;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

public class SurvivalProbabilityService {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SurvivalProbabilityService.class.getName());
  private static final String CACHE_KEY = "survivalProbability";
  private QuizPerformanceRepository quizPerformanceRepository;
  private Cache<String, Map<Integer, Map<Integer, Integer>>> inMemoryCache;

  @Inject
  public SurvivalProbabilityService(
      QuizPerformanceRepository quizPerformanceRepository) {
    this.quizPerformanceRepository = quizPerformanceRepository;
    inMemoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES).build();
  }

  public Result getSurvivalProbability(String quizID, Integer a_from,
      Integer a_to, Integer b_from, Integer b_to) {
    Map<Integer, Map<Integer, Integer>> values = getCachedValues();

    if (values == null) {//empty cache
      return new Result(1L, 1L, 0.5d); // We assume a default survival probability of 0.5
    }
    
    Integer users_from = values.containsKey(a_from) ? values.get(a_from).get(b_from) : null;
    Integer users_to = values.containsKey(a_to) ? values.get(a_to).get(b_to) : null;
    
    if (users_from == null || users_to == null || users_from == 0) { 
      return new Result(1L, 1L, 0.5d); // We assume a default survival probability of 0.5
    }
    
    double psurvival = 1.0 * users_to / users_from;

    return new Result(users_from, users_to, psurvival);
  }

  private Map<Integer, Map<Integer, Integer>> getCachedValues() {
    Map<Integer, Map<Integer, Integer>> result = inMemoryCache
        .getIfPresent(CACHE_KEY);

    if (result == null) {
      result = CachePMF.get(CACHE_KEY, HashMap.class);
      if (result != null) {
        inMemoryCache.put(CACHE_KEY, result);
      }
    }
    return result;
  }

  public void cache() {
    CachePMF.put(CACHE_KEY, quizPerformanceRepository.getAnswersForSurvivalProbability(), 
        60 * 60 * 25);
  }

  public class Result {
    private long users_from;
    private long users_to;
    private double psurvival;

    public Result(long u_from, long u_to, double psurvival) {
      this.users_from = u_from;
      this.users_to = u_to;
      this.psurvival = psurvival;
    }

    public long getU_from() {
      return users_from;
    }

    public void setU_from(long u_from) {
      this.users_from = u_from;
    }

    public long getU_to() {
      return users_to;
    }

    public void setU_to(long u_to) {
      this.users_to = u_to;
    }

    public double getPsurvival() {
      return psurvival;
    }

    public void setPsurvival(double psurvival) {
      this.psurvival = psurvival;
    }
  }
}
