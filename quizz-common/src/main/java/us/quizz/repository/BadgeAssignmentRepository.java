package us.quizz.repository;

import us.quizz.entities.BadgeAssignment;
import us.quizz.ofy.OfyBaseRepository;

public class BadgeAssignmentRepository extends OfyBaseRepository<BadgeAssignment> {
  public BadgeAssignmentRepository() {
    super(BadgeAssignment.class);
  }
}
