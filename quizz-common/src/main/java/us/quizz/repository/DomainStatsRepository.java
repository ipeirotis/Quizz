package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.DomainStats;

public class DomainStatsRepository extends BaseRepository<DomainStats> {
  public DomainStatsRepository() {
    super(DomainStats.class);
  }

  @Override
  protected Key getKey(DomainStats item) {
    return item.getKey();
  }

  @Override
  public void fetchItem(DomainStats browser) {
  }
}
