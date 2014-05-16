package us.quizz.entities;

import java.io.Serializable;

import us.quizz.enums.AnswerKind;

import com.google.appengine.api.datastore.Key;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
//TODO: remove all annotations after migration on all servers, because Answer is embedded entity
@Entity
@Cache
@Index
public class Answer implements Serializable{
  private static final long serialVersionUID = 1L;

  @Id
  private String id;
  @Parent
  private Key parent;
  private Integer internalID;
  private String text;
  private AnswerKind kind;
  private String source;
  private JsonObject metadata;
  private Long questionID;
  private String quizID;
  private Double probability;
  private Integer numberOfPicks;
  // The total number of bits assigned to this answer
  // Calculated as the sum of the average information gain for all users
  // that picked this answer.
  private Double bits;
  // The (estimated/computed) probability that the given answer is correct, 
  // based on the answers from the users.
  private Double probCorrect;
  
  //for Objectify
  @SuppressWarnings("unused")
  private Answer(){}

  public Answer(Long questionID, String quizID, String text, AnswerKind kind,
      Integer internalID) {
    this.questionID = questionID;
    this.quizID = quizID;
    this.text = text;
    this.kind = kind;
    this.internalID = internalID;
    this.id = questionID + quizID + internalID;
  }
  
  public static String generateId(Long questionID, Integer internalID) {
    return "id_" + questionID + "_" + internalID;
  }

  public Integer getInternalID() {
    return internalID;
  }

  public void setInternalID(Integer internalID) {
    this.internalID = internalID;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public AnswerKind getKind() {
    return kind;
  }

  public void setKind(AnswerKind kind) {
    this.kind = kind;
  }

  public void setMetadata(JsonObject metadata) {
    this.metadata = metadata;
  }

  protected JsonPrimitive getPrimitiveMD(String key) {
    return metadata.getAsJsonPrimitive(key);
  }

  public String getStringMetadata(String key) {
    return getPrimitiveMD(key).getAsString();
  }

  public int getIntegerMetadata(String key) {
    return getPrimitiveMD(key).getAsInt();
  }

  public boolean getBoolMetadata(String key) {
    return getPrimitiveMD(key).getAsBoolean();
  }

  public long getLongMetadata(String key) {
    return getPrimitiveMD(key).getAsLong();
  }

  public Double getProbability() {
    return probability;
  }

  public void setProbability(Double probability) {
    this.probability = probability;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public void setQuizID(String quizID) {
    this.quizID = quizID;
  }

  public void setQuestionID(Long questionID) {
    this.questionID = questionID;
  }

  public String getSource() {
    return source;
  }

  public JsonObject getMetadata() {
    return metadata;
  }

  public Long getQuestionID() {
    return questionID;
  }

  public String getQuizID() {
    return quizID;
  }

  public String userAnswerText(String userInput) {
    if (kind != null && kind == AnswerKind.USER_SUBMITTED) {
      return userInput;
    }
    return text;
  }

  public Integer getNumberOfPicks() {
    return numberOfPicks;
  }

  public void setNumberOfPicks(Integer numberOfPicks) {
    this.numberOfPicks = numberOfPicks;
  }

  public Double getBits() {
    return bits;
  }

  public void setBits(Double bits) {
    this.bits = bits;
  }

  public Double getProbCorrect() {
    return probCorrect;
  }

  public void setProbCorrect(Double probCorrect) {
    this.probCorrect = probCorrect;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Key getParent() {
    return parent;
  }

  public void setParent(Key parent) {
    this.parent = parent;
  }
}
