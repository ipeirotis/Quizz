package us.quizz.entities;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class SurvivalProbabilityResult implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private static final int defaultUsersFrom = 100;
  private static final int defaultusersTo = 75;
  private static final double defaultProbSurvival = 0.75;

  public static Key generateKeyFromID(int a_from, int b_from, int c_from, int a_to, int b_to, int c_to) {
    return KeyFactory.createKey(SurvivalProbabilityResult.class.getSimpleName(), "id_" + a_from + "_" + b_from + "_" + c_from + "_"
        + a_to + "_" + b_to + "_" + c_to);
  }

  public static SurvivalProbabilityResult getDefaultResult(Integer a_from, Integer b_from, Integer c_from, Integer a_to, Integer b_to, Integer c_to) {

    boolean isDefault = true;
    
    return new SurvivalProbabilityResult(a_from, b_from, c_from, a_to, b_to, c_to, defaultUsersFrom, defaultusersTo, defaultProbSurvival, isDefault);
   
  }
  
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  // Marks a "default" result whenever we do not have actual numbers to report
  @Persistent
  private Boolean isDefault;

  
  @Persistent
  private Integer correctFrom;
  @Persistent
  private Integer incorrectFrom;
  @Persistent
  private Integer exploitFrom;
  @Persistent
  private Integer correctTo;
  @Persistent
  private Integer incorrectTo;
  @Persistent
  private Integer exploitTo;
  @Persistent
  private Integer usersFrom;
  @Persistent
  private Integer usersTo;
  @Persistent
  private Double probSurvival;
  
  // Last time that we computed the object
  @Persistent
  private Date timestamp;

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public SurvivalProbabilityResult(Integer a_from, Integer b_from, Integer c_from, Integer a_to, Integer b_to, Integer c_to,
      Integer users_from, Integer users_to, double psurvival, boolean isDefault) {
    this.correctFrom = a_from;
    this.incorrectFrom = b_from;
    this.exploitFrom = c_from;
    this.correctTo = a_to;
    this.incorrectTo = b_to;
    this.exploitTo = b_to;

    this.usersFrom = users_from;
    this.usersTo = users_to;
    this.probSurvival = psurvival;
    this.isDefault = isDefault;

    this.key = generateKeyFromID(correctFrom, incorrectFrom, exploitFrom, correctTo, incorrectTo, exploitTo);
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public Integer getCorrectFrom() {
    return correctFrom;
  }

  public void setCorrectFrom(Integer correctFrom) {
    this.correctFrom = correctFrom;
  }

  public Integer getIncorrectFrom() {
    return incorrectFrom;
  }

  public void setIncorrectFrom(Integer incorrectFrom) {
    this.incorrectFrom = incorrectFrom;
  }

  public Integer getExploitFrom() {
    return exploitFrom;
  }

  public void setExploitFrom(Integer exploitFrom) {
    this.exploitFrom = exploitFrom;
  }

  public Integer getCorrectTo() {
    return correctTo;
  }

  public void setCorrectTo(Integer correctTo) {
    this.correctTo = correctTo;
  }

  public Integer getIncorrectTo() {
    return incorrectTo;
  }

  public void setIncorrectTo(Integer incorrectTo) {
    this.incorrectTo = incorrectTo;
  }

  public Integer getExploitTo() {
    return exploitTo;
  }

  public void setExploitTo(Integer exploitTo) {
    this.exploitTo = exploitTo;
  }

  public Integer getUsersFrom() {
    return usersFrom;
  }

  public void setUsersFrom(Integer usersFrom) {
    this.usersFrom = usersFrom;
  }

  public Integer getUsersTo() {
    return usersTo;
  }

  public void setUsersTo(Integer usersTo) {
    this.usersTo = usersTo;
  }

  public Double getProbSurvival() {
    return probSurvival;
  }

  public void setProbSurvival(Double probSurvival) {
    this.probSurvival = probSurvival;
  }

}
