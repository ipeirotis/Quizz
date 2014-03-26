package us.quizz.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.utils.CachePMF;
import us.quizz.utils.MemcacheKey;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;

public class SurvivalProbabilityService {
  // Time to cache survival probability in Memcache.
  private static final int SURVIVAL_PROBABILITIES_CACHED_TIME = 24 * 60;  // 24 hours.

  private QuizPerformanceRepository quizPerformanceRepository;
  private Cache<String, Map<Integer, Map<Integer, Integer>>> inMemoryCache;

  @Inject
  public SurvivalProbabilityService(
      QuizPerformanceRepository quizPerformanceRepository) {
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.inMemoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(SURVIVAL_PROBABILITIES_CACHED_TIME, TimeUnit.MINUTES).build();
  }

  
  
  public Result getSurvivalProbability(String quizID, Integer a_from,
      Integer a_to, Integer b_from, Integer b_to) {
    Map<Integer, Map<Integer, Integer>> values = getCachedValues(quizID);

    if (values == null) {//empty cache
      return new Result(a_from, b_from, a_to, b_to, 100L, 75L, 0.75d); // We assume a default survival probability of 0.5
    }
    
    Integer users_from = values.containsKey(a_from) ? values.get(a_from).get(b_from) : null;
    Integer users_to = values.containsKey(a_to) ? values.get(a_to).get(b_to) : null;
    
    if (users_from == null || users_to == null || users_from == 0) { 
      return new Result(a_from, b_from, a_to, b_to, 100L, 75L, 0.75d); // We assume a default survival probability of 0.5
    }
    
    double psurvival = 1.0 * users_to / users_from;

    return new Result(a_from, b_from, a_to, b_to, users_from, users_to, psurvival);
  }

  public List<Result> getSurvivalProbabilities(String quizID) {
	  	List<Result> result = new ArrayList<Result>();
	  
	    Map<Integer, Map<Integer, Integer>> values = getCachedValues(quizID);
	    if (values == null) return result;

	    int aMax = 0;
	    int bMax = 0;
	    for (int a : values.keySet()) {
	    	if (a>aMax) aMax = a;
	    	Map<Integer, Integer> bs = values.get(a);
		    for (int b : bs.keySet()) {
		    	if (b>bMax) bMax = b;
		    }
	    }
	    
	    for (int a=0; a<aMax; a++) {
		    for (int b=0; b<bMax; b++) {

		    	Result r1 = getSurvivalProbability(quizID,a,a+1,b,b);
		    	result.add(r1);
		    	Result r2 = getSurvivalProbability(quizID,a,a,b,b+1);
		    	result.add(r2);
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
    
    Map<Integer, Map<Integer, Integer>> values  = quizPerformanceRepository.getCountsForSurvivalProbability(quizId);
    
    String key = MemcacheKey.getSurvivalProbabilities(quizId);
    CachePMF.put(key, values, SURVIVAL_PROBABILITIES_CACHED_TIME * 60);
  }

  public class Result {
	  private int correct_from;
	  private int incorrect_from;
	  private int correct_to;
	  private int incorrect_to;
	  
    public int getCorrect_from() {
		return correct_from;
	}

	public void setCorrect_from(int correct_from) {
		this.correct_from = correct_from;
	}

	public int getIncorrect_from() {
		return incorrect_from;
	}

	public void setIncorrect_from(int incorrect_from) {
		this.incorrect_from = incorrect_from;
	}

	public int getCorrect_to() {
		return correct_to;
	}

	public void setCorrect_to(int correct_to) {
		this.correct_to = correct_to;
	}

	public int getIncorrect_to() {
		return incorrect_to;
	}

	public void setIncorrect_to(int incorrect_to) {
		this.incorrect_to = incorrect_to;
	}

	public long getUsers_from() {
		return users_from;
	}

	public void setUsers_from(long users_from) {
		this.users_from = users_from;
	}

	public long getUsers_to() {
		return users_to;
	}

	public void setUsers_to(long users_to) {
		this.users_to = users_to;
	}

	private long users_from;
    private long users_to;
    private double psurvival;

    public Result(int a_from, int b_from, int a_to, int b_to, long u_from, long u_to, double psurvival) {
  	 this.correct_from=a_from;
  	this.incorrect_from=b_from;
  	this.correct_to=a_to;
  	this.incorrect_to=b_to;
  	  
    	
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
