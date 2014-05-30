package us.quizz.utils;

public class UrlUtils {
  public static String extractDomain(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }
    String domain = new String(url);

    // First, strip the protocol part away.
    int index = domain.indexOf("://");
    if (index != -1) {
      domain = domain.substring(index + 3);
    } else {
      return null;
    }

    // Next, strip the rest of the subpath after url away.
    index = domain.indexOf('/');
    if (index != -1) {
      domain = domain.substring(0, index);
    }

    // Finally, strip the www away.
    domain = domain.replaceFirst("^www.*?\\.", "");
    return domain;
  }
}
