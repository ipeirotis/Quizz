package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Badge;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.User;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.BadgeAssignmentRepository;
import us.quizz.repository.BadgeRepository;

import java.util.ArrayList;
import java.util.List;

public class BadgeService extends OfyBaseService<Badge> {
  private BadgeAssignmentRepository badgeAssignmentRepository;

  @Inject
  public BadgeService(BadgeRepository badgeRepository, 
      BadgeAssignmentRepository badgeAssignmentRepository){
    super(badgeRepository);
    this.badgeAssignmentRepository = badgeAssignmentRepository;
  }

  // Checks whether the given user will get new badge assignment based on the number of correct
  // answers and total answers obtained.
  public List<Badge> checkForNewBadges(User user, Integer correctanswers, Integer totalanswers) {
    List<Badge> badges = getBadges(correctanswers, totalanswers);
    for (Badge b : badges) {
      BadgeAssignment ba = new BadgeAssignment(user.getUserid(), b.getBadgename());
      badgeAssignmentRepository.save(ba);
    }
    return badges;
  }

  // Given the number of correct answers and number of total answers, returns a list of
  // badges that should be awarded.
  private List<Badge> getBadges(Integer numCorrectAnswers, Integer numTotalAnswers) {
    List<Badge> badgelist = new ArrayList<Badge>();
    switch (numTotalAnswers) {
      case 1:
        badgelist.add(getBadge("Answered One", "A1"));
        break;
      case 5:
        badgelist.add(getBadge("Answered Five", "A5"));
        break;
      case 10:
        badgelist.add(getBadge("Answered Ten", "A10"));
        break;
      case 25:
        badgelist.add(getBadge("Answered Twenty-Five", "A25"));
        break;
      case 50:
        badgelist.add(getBadge("Answered Fifty", "A50"));
        break;
      default:
        break;
    }

    switch (numCorrectAnswers) {
      case 1:
        badgelist.add(getBadge("One Correct", "C1"));
        break;
      case 5:
        badgelist.add(getBadge("Five Correct", "C5"));
        break;
      case 10:
        badgelist.add(getBadge("Ten Correct", "C10"));
        break;
      case 25:
        badgelist.add(getBadge("Twenty-Five Correct", "C25"));
        break;
      case 50:
        badgelist.add(getBadge("Fifty Correct", "C50"));
        break;
      default:
        break;
    }
    return badgelist;
  }

  // Given a badge name and a badge short name, retrieves the corresponding badge from the
  // badgeRepository, if exist, or create one in the repository if it doesn't exist yet.
  private Badge getBadge(String badgeName, String shortName) {
    Badge badge = get(badgeName);
    if (badge == null) {
      Badge newbadge = new Badge(badgeName, shortName);
      save(newbadge);
      badge = newbadge;
    }
    return badge;
  }
}
