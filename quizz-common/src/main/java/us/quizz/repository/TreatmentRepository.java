package us.quizz.repository;

import us.quizz.entities.Treatment;

import com.google.appengine.api.datastore.Key;

public class TreatmentRepository extends BaseRepository<Treatment>{
	
	public TreatmentRepository() {
		super(Treatment.class);
	}
	
	@Override
	protected Key getKey(Treatment item) {
		return item.getKey();
	}

}