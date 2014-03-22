package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserAnswerFeedback implements Serializable {
  private static final long serialVersionUID = 1L;

  public static Key generateKeyFromID(Long questionID, String userid) {
    return KeyFactory.createKey(UserAnswerFeedback.class.getSimpleName(),
        "id_" + questionID + "_" + userid);
  }

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private Long questionID;

  @Persistent
  private String userid;

  @Persistent
  private Integer userAnswerID;

  @Persistent
  private String userAnswerText;

  @Persistent
  private String userNewBadges;

  @Persistent
  private Boolean isCorrect;

  public String getMessage() {
	return message;
}

public void setMessage(String message) {
	this.message = message;
}

@Persistent
  private String correctAnswerText;
  
  @Persistent
  private String message;

  @Persistent
  private Integer numCorrectAnswers;

  @Persistent
  private Integer numTotalAnswers;

  @Persistent
  private String difficulty;

  public UserAnswerFeedback(Long questionID, String userid,
      Integer userAnswerID, Boolean isCorrect) {
    this.questionID = questionID;
    this.userid = userid;
    this.userAnswerID = userAnswerID;
    this.isCorrect = isCorrect;

    this.key = generateKeyFromID(questionID, userid);
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public Long getQuestionID() {
    return questionID;
  }

  public void setQuestionID(Long questionID) {
    this.questionID = questionID;
  }

  public String getUserid() {
    return userid;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  public Integer getUserAnswerID() {
    return userAnswerID;
  }

  public String getUserAnswerText() {
    return userAnswerText;
  }

  public void setUserAnswerText(String userAnswerText) {
    this.userAnswerText = userAnswerText;
  }

  public String getUserNewBadges() {
    return userNewBadges;
  }

  public void setUserNewBadges(List<Badge> newBadges) {
    String userNewBadges = "";
    for (Badge b : newBadges) {
      userNewBadges += b.getBadgename() + "...";
    }
    this.userNewBadges = userNewBadges;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public void setUserAnswerID(Integer userAnswerID) {
    this.userAnswerID = userAnswerID;
  }

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public String getCorrectAnswerText() {
    return correctAnswerText;
  }

  public void setCorrectAnswerText(String correctAnswerText) {
    this.correctAnswerText = correctAnswerText;
  }

  public Integer getNumCorrectAnswers() {
    return numCorrectAnswers;
  }

  public void setNumCorrectAnswers(Integer numCorrectAnswers) {
    this.numCorrectAnswers = numCorrectAnswers;
  }

  public Integer getNumTotalAnswers() {
    return numTotalAnswers;
  }

  public void setNumTotalAnswers(Integer numTotalAnswers) {
    this.numTotalAnswers = numTotalAnswers;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public void computeDifficulty() {
    if (numTotalAnswers == null ||
        numCorrectAnswers == null ||
        numTotalAnswers == 0) {
      this.difficulty = "--";
      return;
    }

    double d = 1.0 * this.numCorrectAnswers / this.numTotalAnswers;
    NumberFormat percentFormat = NumberFormat.getPercentInstance();
    percentFormat.setMaximumFractionDigits(0);
    this.difficulty = percentFormat.format(d);
  }
}
