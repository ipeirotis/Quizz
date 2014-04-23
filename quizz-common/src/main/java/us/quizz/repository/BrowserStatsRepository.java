package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.BrowserStats;

public class BrowserStatsRepository extends BaseRepository<BrowserStats> {
  public BrowserStatsRepository() {
    super(BrowserStats.class);
  }

  @Override
  protected Key getKey(BrowserStats item) {
    return item.getKey();
  }
}
