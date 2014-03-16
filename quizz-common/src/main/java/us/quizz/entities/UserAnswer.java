package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

import us.quizz.enums.AnswerChallengeStatus;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserAnswer implements Serializable {
  private static final long serialVersionUID = 1L;

  public static Key generateKeyFromID(String questionID, String userID) {
    return KeyFactory.createKey(UserAnswer.class.getSimpleName(), "id_"
        + questionID + "_" + userID);
  }

  @Persistent
  private String userid;

  @Persistent
  private Long timestamp;

  @Persistent
  private String ipaddress;

  @Persistent
  private Integer answerID;

  @Persistent
  private String userInput;

  @Persistent
  private Double score;

  @Persistent
  private String referer;

  @Persistent
  private Long questionID;

  @Persistent
  private String quizID;

  @Persistent
  private String browser;

  @Persistent
  private String action;

  @Persistent
  private Boolean isCorrect;

  @Persistent
  private Text answerChallengeText;

  @Persistent
  private AnswerChallengeStatus answerChallengeStatus;

  @Persistent
  private Double answerChallengeWeight = 0.0d;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  public UserAnswer(String userid, Long questionID, Integer useranswerID) {
    this.questionID = questionID;
    this.answerID = useranswerID;
    this.userid = userid;
  }

  public UserAnswer(String userid, String questionID, String answerID) {
    this(userid, Long.parseLong(questionID), Integer.parseInt(answerID));
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

  public Key getKey() {
    return key;
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
}
