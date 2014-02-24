package us.quizz.repository;

import us.quizz.entities.BrowserStats;

import com.google.appengine.api.datastore.Key;

public class BrowserStatsRepository extends BaseRepository<BrowserStats>{
	
	public BrowserStatsRepository() {
		super(BrowserStats.class);
	}
	
	@Override
	protected Key getKey(BrowserStats item) {
		return item.getKey();
	}
	
	@Override
	public void fetchItem(BrowserStats browser) {
	}

}
