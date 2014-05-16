package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

@Entity
@Cache
@Index
public class User implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Integer INITIAL_CHALLENGE_BUDGET = 3;

  @Id
  private String userid;
  private String sessionid;
  private String socialid;
  private Integer challengeBudget;
  private Long experimentId;

  //for Objectify
  @SuppressWarnings("unused")
  private User(){}

  public User(String userid) {
    this.userid = userid;
    this.challengeBudget = INITIAL_CHALLENGE_BUDGET;
  }

  public String getUserid() {
    return userid;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  public String getSocialid() {
    return socialid;
  }

  public void setSocialid(String sid) {
    this.socialid = sid;
  }

  public String getSessionid() {
    return sessionid;
  }

  public void setSessionid(String sessionid) {
    this.sessionid = sessionid;
  }

  public Integer getChallengeBudget() {
    return challengeBudget;
  }

  public void setChallengeBudget(Integer challengeBudget) {
    this.challengeBudget = challengeBudget;
  }

  public void incChallengeBudget() {
    this.challengeBudget++;
  }

  public void decChallengeBudget() {
    this.challengeBudget--;
  }

  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }
}
