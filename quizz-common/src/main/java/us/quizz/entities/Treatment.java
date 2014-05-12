package us.quizz.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * The treatment object assigns a set of **boolean** variables that indicate
 * which treatments are active for the given user. At this point, we only
 * support boolean treatments. In the future we plan to add support for enum
 * fields as treatments.
 */
@Entity
@Cache
@Index
public class Treatment implements Serializable {
  private static final long serialVersionUID = 1L;

  // The name of the treatment
  @Id
  private String name;

  // The probability of assigning this treatment to a user
  private Double probability;

  // This is to allow for the treatment to block other treatments
  // within the same experiment
  private Set<String> blocks;

  // This is to allow for the treatment to be blocked by other treatments
  // within the same experiment
  private Set<String> blockedBy;

  // If this treatment needs to run by itself
  private Boolean blocksAll;

  //for Objectify
  @SuppressWarnings("unused")
  private Treatment(){}

  public Treatment(String name, Double probability) {
    this.name = name;
    this.probability = probability;
    this.blocks = new HashSet<String>();
    this.blockedBy = new HashSet<String>();
    this.blocksAll = false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getProbability() {
    return probability;
  }

  public void setProbability(Double probability) {
    this.probability = probability;
  }

  public Set<String> getBlocks() {
    return blocks;
  }

  public void setBlocks(Set<String> blocks) {
    this.blocks = blocks;
  }

  public Set<String> getBlockedBy() {
    return blockedBy;
  }

  public void setBlockedBy(Set<String> blockedBy) {
    this.blockedBy = blockedBy;
  }

  public Boolean getBlocksAll() {
    return blocksAll;
  }

  public void setBlocksAll(Boolean blocksAll) {
    this.blocksAll = blocksAll;
  }
}
