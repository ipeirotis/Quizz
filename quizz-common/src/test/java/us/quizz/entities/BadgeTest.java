package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BadgeTest {
  @Test
  public void testConstructor() {
    String badgename = "5_Correct";
    String shortname = "5C";
    Badge badge = new Badge(badgename, shortname);
    assertEquals(badgename, badge.getBadgename());
    assertEquals(shortname, badge.getShortname());
  }
} 

