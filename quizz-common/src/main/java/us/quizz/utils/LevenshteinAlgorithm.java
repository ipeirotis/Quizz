package us.quizz.utils;

public class LevenshteinAlgorithm {
  public static int getLevenshteinDistance(String s1, String s2) {
    if (s1 == null || s2 == null) {
      throw new IllegalArgumentException("Strings must not be null");
    }

    int len1 = s1.length();
    int len2 = s2.length();
    if (len1 == 0) {
      return len2;
    } else if (len2 == 0) {
      return len1;
    }

    if (len1 > len2) {
      // swap the input strings to consume less memory
      String tmp = s1;
      s1 = s2;
      s2 = tmp;
      len1 = len2;
      len2 = s2.length();
    }

    int p[] = new int[len1 + 1];  // 'previous' cost array, horizontally
    int d[] = new int[len1 + 1];  // cost array, horizontally
    int _d[];  // placeholder to assist in swapping p and d.

    // Initialize cost array to be the number of characters to delete from s1.
    for (int i = 0; i <= len1; i++) {
      p[i] = i;
    }
    for (int j = 1; j <= len2; j++) {
      char s2c = s2.charAt(j - 1);
      // delete j characters from s2 to match s1.
      d[0] = j;

      for (int i = 1; i <= len1; i++) {
        int cost = s1.charAt(i - 1) == s2c ? 0 : 1;
        // Minimum of either of the following:
        // - addition of a new character (d[i - 1] + 1).
        // - deletion of a character (p[i] + 1).
        // - substitution of the character (p[i - 1] + cost).
        d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
      }

      // copy current distance counts to 'previous row' distance counts
      _d = p;
      p = d;
      d = _d;
    }

    // our last action in the above loop was to switch d and p, so p now
    // actually has the most recent cost counts
    return p[len1];
  }
}
