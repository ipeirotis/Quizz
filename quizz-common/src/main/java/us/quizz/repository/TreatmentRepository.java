package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.Treatment;

public class TreatmentRepository extends BaseRepository<Treatment> {
  public TreatmentRepository() {
    super(Treatment.class);
  }

  @Override
  protected Key getKey(Treatment item) {
    return item.getKey();
  }
}
