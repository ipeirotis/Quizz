package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.SurvivalProbabilityResult;

public class SurvivalProbabilityResultRepository extends BaseRepository<SurvivalProbabilityResult> {

  public SurvivalProbabilityResultRepository() {
    super(SurvivalProbabilityResult.class);
  }

  @Override
  protected Key getKey(SurvivalProbabilityResult item) {
    return item.getKey();
  }

}
