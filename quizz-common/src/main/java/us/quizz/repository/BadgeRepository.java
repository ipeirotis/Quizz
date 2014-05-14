package us.quizz.repository;

import us.quizz.entities.Badge;
import us.quizz.ofy.OfyBaseRepository;

public class BadgeRepository extends OfyBaseRepository<Badge> {
  public BadgeRepository() {
    super(Badge.class);
  }
}
