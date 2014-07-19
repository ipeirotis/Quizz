package us.quizz.entities;

import com.google.common.base.Preconditions;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import us.quizz.enums.QuestionSelectionStrategy;

import java.io.Serializable;
import java.lang.Math;
import java.util.Random;

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

  // If this user is participating in a quizz that has allowVaryingLengthQuizSession set to true,
  // then this is the num questions to be shown to a user in a single quiz session, after which the
  // score is tallied. If numQuestionsLimit is negative, it means there is no limit to the num
  // questions.
  private Integer numQuestionsLimit;

  @Ignore
  private Random rand;

  //for Objectify
  @SuppressWarnings("unused")
  private User(){}

  public User(String userid) {
    this(userid, new Random());
  }

  public User(String userid, Random rand) {
    this.userid = userid;
    this.challengeBudget = INITIAL_CHALLENGE_BUDGET;
    this.rand = rand;

    switch(this.rand.nextInt(6)) {
      case 0:
        numQuestionsLimit = 3;
        break;
      case 1:
        numQuestionsLimit = 5;
        break;
      case 2:
        numQuestionsLimit = 7;
        break;
      case 3:
        numQuestionsLimit = 10;
        break;
      case 4:
        numQuestionsLimit = 20;
        break;
      case 5:
        numQuestionsLimit = -1;
        break;
      default:
        break;
    }
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

  public Integer getNumQuestionsLimit() {
    return this.numQuestionsLimit;
  }

  public void setNumQuestionsLimit(Integer numQuestionsLimit) {
    Preconditions.checkNotNull(numQuestionsLimit, "numQuestionsLimit cannot be null.");
    this.numQuestionsLimit = numQuestionsLimit;
  }

  /**
   * Pick a strategy based on the userID
   * TODO(kobren): implement a better way of assigning Users to question selection strategies
   */
  public QuestionSelectionStrategy pickQuestionSelectionStrategy() {
    if (selectionStrategy == null) {
      selectionStrategy = QuestionSelectionStrategy.values()[
          Math.abs(userid.hashCode()) % QuestionSelectionStrategy.values().length];
    }
    return selectionStrategy;
  }
}
