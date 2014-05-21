package us.quizz.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BadgeAssignmentTest {
  @Test
  public void testConstructor() {
    String userid = "12345";
    String badgename = "5_Correct";
    BadgeAssignment badgeAssignment = new BadgeAssignment(userid, badgename);
    assertEquals(userid, badgeAssignment.getUserid());
    assertEquals(badgename, badgeAssignment.getBadgename());
    assertEquals("id_12345_5_Correct", badgeAssignment.getId());
  }
} 
