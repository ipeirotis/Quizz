package us.quizz.utils;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CachePMF {
  public static int DEFAULT_LIFETIME = 24 * 3600;

  public static <T> void put(String key, T obj) {
    put(key, obj, DEFAULT_LIFETIME);
  }

  public static <T> void put(String key, T obj, int seconds) {
    Map<Integer, Integer> props = new HashMap<Integer, Integer>();
    props.put(GCacheFactory.EXPIRATION_DELTA, seconds);

    try {
      CacheFactory cacheFactory = CacheManager.getInstance()
          .getCacheFactory();
      Cache cache = cacheFactory.createCache(props);
      cache.put(key, obj);
    } catch (CacheException e) {
      PrintStream printStream = new PrintStream(System.err, true);
      e.printStackTrace(printStream);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String key, Class<T> type) {
    T result = null;
    try {
      CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
      Cache cache = cacheFactory.createCache(Collections.emptyMap());
      if (cache != null && cache.containsKey(key)) {
        result = (T) cache.get(key);
      }
    } catch (CacheException e) {
      PrintStream printStream = new PrintStream(System.err, true);
      e.printStackTrace(printStream);
    }

    return result;
  }
}
