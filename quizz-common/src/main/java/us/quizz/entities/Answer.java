package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import us.quizz.enums.AnswerKind;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Answer implements Serializable{
  private static final long serialVersionUID = 1L;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key id;

  @Persistent
  private Integer internalID;

  // contains the text/html to display
  @Persistent
  private String text;
 
  // Used to identify what to expect in metadata and how to interpret the score
  @Persistent
  private AnswerKind kind;
  
  // The source of the answer. Mainly used for SILVER answers and refers to KV
  @Persistent
  private String source;

  @Persistent
  private JsonObject metadata;

  @Persistent
  private Long questionID;

  @Persistent
  private String quizID;

  // If this is a SILVER answer, the probability that it is correct
  @Persistent
  private Double probability;
  
  // The number of times that users have selected this answer
  @Persistent
  private Long numberOfPicks;
  
  // The total number of bits assigned to this answer
  // Calculated as the sum of the average information gain for all users
  // that picked this answer.
  @Persistent
  private Double bits;
  
  public Double getProbCorrect() {
    return probCorrect;
  }

  public void setProbCorrect(Double probCorrect) {
    this.probCorrect = probCorrect;
  }

  // The (estimated/computed) probability that the given answer is correct, 
  // based on the answers from the users.
  @Persistent
  private Double probCorrect;

  public Answer(Long questionID, String quizID, String text, AnswerKind kind,
      Integer internalID) {
    this.questionID = questionID;
    this.quizID = quizID;
    this.text = text;
    this.kind = kind;
    this.internalID = internalID;
    this.id = generateKeyFromID(questionID, internalID);
  }
  
  public static String generateKeyID(Long questionID, Integer internalID) {
    return "id_" + questionID + "_" + internalID;
  }

  public static Key generateKeyFromID(Long questionID, Integer internalID) {
    return generateKeyFromKeyID(generateKeyID(questionID, internalID));
  }

  public static Key generateKeyFromKeyID(String keyID) {
    return KeyFactory.createKey(Answer.class.getSimpleName(), keyID);
  }

  public Key getID() {
    return id;
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

  public void setId(Key id) {
    this.id = id;
  }

  public String userAnswerText(String userInput) {
    if (kind != null && kind == AnswerKind.USER_SUBMITTED) {
      return userInput;
    }
    return text;
  }

  public Long getNumberOfPicks() {
    return numberOfPicks;
  }

  public void setNumberOfPicks(Long numberOfPicks) {
    this.numberOfPicks = numberOfPicks;
  }

  public Double getBits() {
    return bits;
  }

  public void setBits(Double bits) {
    this.bits = bits;
  }
}
