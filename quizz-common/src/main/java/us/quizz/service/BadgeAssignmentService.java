package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Badge;
import us.quizz.entities.BadgeAssignment;
import us.quizz.entities.User;
import us.quizz.repository.BadgeAssignmentRepository;

public class BadgeAssignmentService {
  private BadgeAssignmentRepository badgeAssignmentRepository;

  @Inject
  public BadgeAssignmentService(BadgeAssignmentRepository badgeAssignmentRepository){
    this.badgeAssignmentRepository = badgeAssignmentRepository;
  }

  public void save(BadgeAssignment badgeAssignment){
    badgeAssignmentRepository.save(badgeAssignment);
  }

  public Boolean userHasBadge(User u, Badge b) {
    BadgeAssignment ba = badgeAssignmentRepository.get(
        BadgeAssignment.generateId(u.getUserid(), b.getBadgename()));
    return (ba != null);
  }
}
