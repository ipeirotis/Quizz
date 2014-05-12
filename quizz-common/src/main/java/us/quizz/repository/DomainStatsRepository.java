package us.quizz.repository;

import us.quizz.entities.DomainStats;
import us.quizz.ofy.OfyBaseRepository;

public class DomainStatsRepository extends OfyBaseRepository<DomainStats> {
  public DomainStatsRepository() {
    super(DomainStats.class);
  }
}
