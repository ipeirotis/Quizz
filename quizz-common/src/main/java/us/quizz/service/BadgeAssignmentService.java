package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Badge;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.User;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.BadgeAssignmentRepository;

public class BadgeAssignmentService extends OfyBaseService<BadgeAssignment> {
  @Inject
  public BadgeAssignmentService(BadgeAssignmentRepository badgeAssignmentRepository){
    super(badgeAssignmentRepository);
  }

  public Boolean userHasBadge(User u, Badge b) {
    BadgeAssignment ba = get(BadgeAssignment.generateId(u.getUserid(), b.getBadgename()));
    return (ba != null);
  }
}
