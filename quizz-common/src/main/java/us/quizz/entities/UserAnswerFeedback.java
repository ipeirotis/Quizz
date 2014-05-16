package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;

@Entity
@Cache
@Index
public class UserAnswerFeedback implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String id;
  private Long questionID;
  private String userid;
  private Integer userAnswerID;
  private String userAnswerText;
  private String userNewBadges;
  private Boolean isCorrect;
  private String correctAnswerText;
  private String message;
  private Integer numCorrectAnswers;
  private Integer numTotalAnswers;
  private String difficulty;

  public UserAnswerFeedback(Long questionID, String userid,
      Integer userAnswerID, Boolean isCorrect) {
    this.id = UserAnswerFeedback.generateId(questionID, userid);
    this.questionID = questionID;
    this.userid = userid;
    this.userAnswerID = userAnswerID;
    this.isCorrect = isCorrect;
  }

  public static String generateId(Long questionID, String userID) {
    return questionID + "_" + userID;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
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
