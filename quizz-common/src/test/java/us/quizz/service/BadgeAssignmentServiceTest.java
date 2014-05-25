package us.quizz.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import us.quizz.entities.Badge;
import us.quizz.entities.User;
import us.quizz.utils.QuizBaseTest;

@RunWith(JUnit4.class)
public class BadgeAssignmentServiceTest extends QuizBaseTest {
  @Before
  public void setUp() {
    super.setUp();
    initBadgeAssignmentService();
  }

  @Test
  public void testUserHasBadge() {
    User user = new User(USER_ID1);
    Badge badge = new Badge(BADGE_NAME1, BADGE_SHORTNAME1);
    assertTrue(badgeAssignmentService.userHasBadge(user, badge));

    user = new User(USER_ID2);
    assertFalse(badgeAssignmentService.userHasBadge(user, badge));
  }
}
