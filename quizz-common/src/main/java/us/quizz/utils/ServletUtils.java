package us.quizz.utils;

import javax.servlet.http.HttpServletRequest;

public class ServletUtils {
  public static void ensureParameters(HttpServletRequest request, String... params) {
    for (String param : params) {
      if (request.getParameter(param) == null) {
        throw new IllegalArgumentException("Missing parameter: " + param);
      }
    }
  }
}
