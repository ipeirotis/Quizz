package us.quizz.utils;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.PrintStream;

public class CachePMF {
  public static int DEFAULT_LIFETIME = 24 * 3600;

  public static <T> void put(String key, T obj) {
    put(key, obj, DEFAULT_LIFETIME);
  }

  public static <T> void put(String key, T obj, int seconds) {
    try {
      MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
      memcacheService.put(key, obj, Expiration.byDeltaSeconds(seconds));
    } catch (MemcacheServiceException e) {
      PrintStream printStream = new PrintStream(System.err, true);
      e.printStackTrace(printStream);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String key, Class<T> type) {
    T result = null;
    try {
      MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
      if (memcacheService.contains(key)) {
        result = (T) memcacheService.get(key);
      }
    } catch (MemcacheServiceException e) {
      PrintStream printStream = new PrintStream(System.err, true);
      e.printStackTrace(printStream);
    }

    return result;
  }
}
