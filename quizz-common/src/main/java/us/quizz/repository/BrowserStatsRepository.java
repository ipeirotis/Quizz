package us.quizz.repository;

import us.quizz.entities.BrowserStats;
import us.quizz.ofy.OfyBaseRepository;

public class BrowserStatsRepository extends OfyBaseRepository<BrowserStats> {
  public BrowserStatsRepository() {
    super(BrowserStats.class);
  }
}
