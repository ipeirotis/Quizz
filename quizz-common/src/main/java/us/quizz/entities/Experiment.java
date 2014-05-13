package us.quizz.entities;

import static us.quizz.ofy.OfyService.ofy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * An experiment is a set of treatments that gets assigned to a user.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Experiment implements Serializable {
  private static final long serialVersionUID = 1L;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  // A map that shows whether a particular treatment is active or not
  // within the experiment. If a particular treatment does not appear
  // within the map, we assume it is inactive
  @Persistent(defaultFetchGroup = "true")
  HashMap<String, Boolean> treatments;

  public Experiment() {
    assignTreatments();
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public HashMap<String, Boolean> getTreatments() {
    return treatments;
  }

  public void setTreatments(HashMap<String, Boolean> treatments) {
    this.treatments = treatments;
  }

  public boolean getsTreatment(String treatmentName) {
    if (this.treatments == null) {
      assignTreatments();
    }
    if (this.treatments.containsKey(treatmentName)) {
      Boolean active = this.treatments.get(treatmentName);
      if (active == null) {
        return false;
      } else {
        return active;
      }
    } else {
      return false;
    }
  }

  public void assignTreatments() {
    // Going over all the active treatments in the datastore and assign
    // treatments according to their probabilities.

    // At this point, we do not use/support the blocking functionality
    List<Treatment> allTreatments = ofy().load().type(Treatment.class).list();

    this.treatments = new HashMap<String, Boolean>();
    for (Treatment t : allTreatments) {
      Boolean activate = (Math.random() < t.getProbability());
      this.treatments.put(t.getName(), activate);
    }
  }
}
