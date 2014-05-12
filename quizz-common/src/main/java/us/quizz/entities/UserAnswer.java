package us.quizz.entities;

import java.io.Serializable;

import us.quizz.enums.AnswerChallengeStatus;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
@Index
public class UserAnswer implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private Long id;
  private String userid;
  private Long timestamp;
  private String ipaddress;
  private Integer answerID;
  private String userInput;
  private Double score;
  private String referer;
  private Long questionID;
  private String quizID;
  private String browser;
  private String action;
  private Boolean isCorrect;
  private Text answerChallengeText;
  private AnswerChallengeStatus answerChallengeStatus;
  private Double answerChallengeWeight = 0.0d;

  //for Objectify
  @SuppressWarnings("unused")
  private UserAnswer(){}

  public UserAnswer(String userid, Long questionID, Integer useranswerID) {
    this.questionID = questionID;
    this.answerID = useranswerID;
    this.userid = userid;
  }

  public UserAnswer(String userid, String questionID, String answerID) {
    this(userid, Long.parseLong(questionID), Integer.parseInt(answerID));
  }

  public UserAnswer(String userID, Long questionID, Integer answerID, String quizID) {
    this(userID, questionID, answerID);
    this.quizID = quizID;
  }

  public String getAction() {
    return action;
  }

  public Integer getAnswerID() {
    return answerID;
  }

  public String getBrowser() {
    return browser;
  }

  public Boolean getCorrect() {
    return isCorrect;
  }

  public String getIpaddress() {
    return ipaddress;
  }

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public Long getQuestionID() {
    return questionID;
  }

  public String getQuizID() {
    return quizID;
  }

  public String getReferer() {
    return referer;
  }

  public Double getScore() {
    return score;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getUserid() {
    return userid;
  }

  public String getUserInput() {
    return userInput;
  }

  public void setAction(String action) {
    this.action = action.replace('\t', ' ');
  }

  public void setAnswerID(Integer answerID) {
    this.answerID = answerID;
  }

  public void setBrowser(String browser) {
    this.browser = browser.replace('\t', ' ');
  }

  public void setCorrect(Boolean correct) {
    isCorrect = correct;
  }

  public void setIpaddress(String ipaddress) {
    this.ipaddress = ipaddress.replace('\t', ' ');
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public void setQuestionID(Long questionID) {
    this.questionID = questionID;
  }

  public void setQuizID(String quizID) {
    this.quizID = quizID;
  }

  public void setReferer(String referer) {
    this.referer = referer.replace('\t', ' ');
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public void setUserid(String userid) {
    this.userid = userid.replace('\t', ' ');
  }

  public void setUserInput(String userInput) {
    this.userInput = userInput;
  }

  public Double getAnswerChallengeWeight() {
    return answerChallengeWeight;
  }

  public void setAnswerChallengeWeight(Double answerChallengeWeight) {
    this.answerChallengeWeight = answerChallengeWeight;
  }

  public AnswerChallengeStatus getAnswerChallengeStatus() {
    return answerChallengeStatus;
  }

  public void setAnswerChallengeStatus(AnswerChallengeStatus answerChallengeStatus) {
    this.answerChallengeStatus = answerChallengeStatus;
  }

  public Text getAnswerChallengeText() {
    return answerChallengeText;
  }

  public void setAnswerChallengeText(Text answerChallengeText) {
    this.answerChallengeText = answerChallengeText;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
