package us.quizz.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UrlUtilsTest {
  @Test
  public void testExtractDomain() {
    assertNull(UrlUtils.extractDomain(null));
    assertNull(UrlUtils.extractDomain(""));
    assertNull(UrlUtils.extractDomain("aaa"));
    assertEquals("google.com", UrlUtils.extractDomain("http://www.google.com"));
    assertEquals("google.com", UrlUtils.extractDomain("https://www.google.com"));
    assertEquals("google.com", UrlUtils.extractDomain("http://google.com"));
    assertEquals("google.com", UrlUtils.extractDomain("http://www.google.com/some/sub/path?a=1"));
  }
}
