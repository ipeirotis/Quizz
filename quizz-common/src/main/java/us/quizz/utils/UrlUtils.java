package us.quizz.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

public class UrlUtils {
  private static final List<String> URL_PARAMS = Arrays.asList("url");

  public static String extractUrl(String referer) {
    if (referer != null && !referer.isEmpty()) {
      try {
        for (String pair : referer.split("&")) {
          int idx = pair.indexOf('=');
          if(idx > 0) {
            String name = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
            for (String param : URL_PARAMS) {
              if (param.equals(name)) {
                return URLDecoder.decode(pair.substring(idx+1), "UTF-8");
              }
            }
          }
        }
      } catch (UnsupportedEncodingException e) {
        throw new IllegalArgumentException(e);
      }
    }    
    return referer;
  }

  public static String extractDomain(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }
    String domain = new String(url);
    int index = domain.indexOf("://");

    if (index != -1) {
      domain = domain.substring(index + 3);
    }

    index = domain.indexOf('/');

    if (index != -1) {
      domain = domain.substring(0, index);
    }

    domain = domain.replaceFirst("^www.*?\\.", "");
    return domain;
  }

  public static String extractUrlAndDomain(String referer) {
    return extractDomain(extractUrl(referer));
  }
}
