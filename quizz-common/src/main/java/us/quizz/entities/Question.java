package us.quizz.entities;

import java.io.Serializable;
import java.util.ArrayList;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuizKind;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Question implements Serializable {
  private static final long serialVersionUID = 1L;
  
  @Persistent
  private String quizID;

  @Persistent
  private Double weight;

  @Persistent
  private String text;
  
  @Persistent
  private QuizKind kind;

  @Persistent
  private Long adGroupId;

  @Persistent
  private Long adTextId;

  @Persistent
  private Integer numberOfUserAnswers;

  @Persistent
  private Boolean hasUserAnswers;

  @Persistent
  private Double totalUserScore;

  @Persistent
  private Boolean hasGoldAnswer;

  @Persistent
  private Integer numberOfGoldAnswers;

  @Persistent
  private Boolean hasSilverAnswers;

  @Persistent
  private Integer numberOfSilverAnswers;

  @Persistent
  private Integer numberOfCorrentUserAnswers;

  @Persistent(defaultFetchGroup = "true")
  private ArrayList<Answer> answers;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;
  
  public Question(String quizID, String text, QuizKind kind, Double weight) {
    this.quizID = quizID;
    this.weight = weight;
    this.text = text;
    this.kind = kind;
    this.answers = new ArrayList<Answer>();
  }
  
  public static Key generateKeyFromID(Long id) {
    return KeyFactory.createKey(Question.class.getSimpleName(), id);
  }

  public String getQuizID() {
    return quizID;
  }

  public void setQuizID(String quizID) {
    this.quizID = quizID;
  }

  public Integer getNumberOfGoldAnswers() {
    return numberOfGoldAnswers;
  }

  public void setNumberOfGoldAnswers(Integer numberOfGoldAnswers) {
    this.numberOfGoldAnswers = numberOfGoldAnswers;
  }

  public Integer getNumberOfSilverAnswers() {
    return numberOfSilverAnswers;
  }

  public void setNumberOfSilverAnswers(Integer numberOfSilverAnswers) {
    this.numberOfSilverAnswers = numberOfSilverAnswers;
  }

  public Boolean getHasGoldAnswer() {
    return hasGoldAnswer;
  }

  public Boolean getHasSilverAnswers() {
    return hasSilverAnswers;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public Long getAdGroupId() {
    return adGroupId;
  }

  public Long getAdTextId() {
    return adTextId;
  }

  public Key getKey() {
    return key;
  }

  public Long getID() {
    return key.getId();
  }

  /**
   * @return the weight
   */
  public Double getWeight() {
    return weight;
  }

  public void setAdGroupId(Long adGroupId) {
    this.adGroupId = adGroupId;
  }

  public void setAdTextId(Long adTextId) {
    this.adTextId = adTextId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
  
  public QuizKind getKind() {
    return kind;
  }

  public void setKind(QuizKind kind) {
    this.kind = kind;
  }

  public void setRelation(String quizID) {
    this.quizID = quizID;
  }

  public void setWeight(Double weight) {
    this.weight = weight;
  }

  public Boolean hasUserAnswers() {
    return numberOfUserAnswers > 0;
  }

  public Integer getNumberOfUserAnswers() {
    return numberOfUserAnswers;
  }

  public void setNumberOfUserAnswers(Integer numberOfUserAnswers) {
    this.numberOfUserAnswers = numberOfUserAnswers;
  }

  public Integer getNumberOfCorrentUserAnswers() {
    return numberOfCorrentUserAnswers;
  }

  public void setNumberOfCorrentUserAnswers(Integer numberOfCorrentUserAnswers) {
    this.numberOfCorrentUserAnswers = numberOfCorrentUserAnswers;
  }

  public Double getTotalUserScore() {
    return totalUserScore;
  }

  public void setTotalUserScore(Double totalUserScore) {
    this.totalUserScore = totalUserScore;
  }

  public void setHasGoldAnswer(Boolean hasGoldAnswer) {
    this.hasGoldAnswer = hasGoldAnswer;
  }

  public void setHasSilverAnswers(Boolean hasSilverAnswers) {
    this.hasSilverAnswers = hasSilverAnswers;
  }

  public void setHasUserAnswers(Boolean hasUserAnswers) {
    this.hasUserAnswers = hasUserAnswers;
  }

  public Boolean getHasUserAnswers() {
    return hasUserAnswers;
  }

  public ArrayList<Answer> getAnswers() {
    return answers;
  }

  public void setAnswers(ArrayList<Answer> answers) {
    this.answers = answers;
  }

  public void addAnswer(Answer answer) {
    answers.add(answer);
  }

  public Answer getAnswer(Integer answerID) {
    return answers.get(answerID);
  }

  public Answer goldAnswer() {
    // check if there's any designate one of the golds as the feedback
    // answer i.e. feedback_gold
    for (final Answer answer : answers) {
      if (answer.getKind() == AnswerKind.FEEDBACK_GOLD) {
        return answer;
      }
    }
    // if no feedback gold is there return first gold answer
    for (final Answer answer : answers) {
      if (answer.getKind()  == AnswerKind.GOLD) {
        return answer;
      }
    }
    // If no gold answer, return silver answer.
    for (final Answer answer : answers) {
      if (answer.getKind() == AnswerKind.SILVER) {
        return answer;
      }
    }
    throw new IllegalArgumentException(
        "This question doesn't have any gold or silver answer");
  }

}
