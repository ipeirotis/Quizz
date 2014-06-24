package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import us.quizz.enums.QuestionSelectionStrategy;

import java.io.Serializable;

@Entity
@Cache
@Index
public class User implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Integer INITIAL_CHALLENGE_BUDGET = 3;

  @Id
  private String userid;
  private Integer challengeBudget;
  private Long experimentId;
  private QuestionSelectionStrategy selectionStrategy;

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

  /**
   * Pick a strategy based on the userID
   * TODO(kobren): implement a better way of assigning Users to question selection strategies
   */
  public QuestionSelectionStrategy pickQuestionSelectionStrategy() {
    if (selectionStrategy == null) {
      selectionStrategy = QuestionSelectionStrategy.values()[
          userid.hashCode() % QuestionSelectionStrategy.values().length];
    }
    return selectionStrategy;
  }
}
