package us.quizz.entities;

import java.io.Serializable;
import java.util.Map;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * An experiment is a set of treatments that gets assigned to a user.
 */
@Entity
@Cache
@Index
public class Experiment implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private Long id;
  // A map that shows whether a particular treatment is active or not
  // within the experiment. If a particular treatment does not appear
  // within the map, we assume it is inactive
  private Map<String, Boolean> treatments;

  public Map<String, Boolean> getTreatments() {
    return treatments;
  }

  public void setTreatments(Map<String, Boolean> treatments) {
    this.treatments = treatments;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
