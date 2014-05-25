package us.quizz.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Badge;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.User;
import us.quizz.utils.QuizBaseTest;

import java.util.List;

@RunWith(JUnit4.class)
public class BadgeServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initBadgeService();
  }

  @Test
  public void testCheckForNewBadgesEmpty() {
    User user = new User(USER_ID1);
    List<Badge> newBadges = badgeService.checkForNewBadges(user, 3, 15);
    assertEquals(0, newBadges.size());
  }

  @Test
  public void testCheckForNewBadges() {
    assertNull(badgeAssignmentRepository.get(
        BadgeAssignment.generateId(USER_ID1, "Answered Ten")));
    assertNull(badgeAssignmentRepository.get(
        BadgeAssignment.generateId(USER_ID1, "Five Correct")));
    assertNull(badgeRepository.get("Answered Ten"));
    assertNull(badgeRepository.get("Five Correct"));

    User user = new User(USER_ID1);
    List<Badge> newBadges = badgeService.checkForNewBadges(user, 5, 10);
    assertEquals(2, newBadges.size());
    assertNotNull(badgeAssignmentRepository.get(
        BadgeAssignment.generateId(USER_ID1, "Answered Ten")));
    assertNotNull(badgeAssignmentRepository.get(
        BadgeAssignment.generateId(USER_ID1, "Five Correct")));
    assertNotNull(badgeRepository.get("Answered Ten"));
    assertNotNull(badgeRepository.get("Five Correct"));
  }
}
