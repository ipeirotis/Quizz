package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Stringify;

import us.quizz.enums.AnswerAggregationStrategy;
import us.quizz.enums.AnswerKind;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Answer implements Serializable{
  private static final long serialVersionUID = 1L;

  @Parent
  private Key parent;
  private Integer internalID;
  private String text;
  private AnswerKind kind;
  private String source;
  private JsonObject metadata;
  private Long questionID;
  private String quizID;
  
  // Text for showing context for the particular answer
  private Text helpText;

  // The prior probability that this answer is correct, given by the client.
  private Double probability;

  // Number of times this answer is picked by users.
  private Integer numberOfPicks;

  // The total number of bits assigned to this answer
  // Calculated as the sum of the average information gain for all users
  // that picked this answer.
  private Double bits;

  // The posterior probabilities that a given answer is correct
  // for a given strategy, which is represented as String (and is 
  // an enum in the AnswerAggregationStrategy)
  private Map<String, Double> probCorrects;
  
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
    this.probCorrects = new HashMap<String, Double>();
  }

  public Answer(String text, AnswerKind kind) {
    this.text = text;
    this.kind = kind;
    this.probCorrects = new HashMap<String, Double>();
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

  public Key getParent() {
    return parent;
  }

  public void setParent(Key parent) {
    this.parent = parent;
  }

  public Text getHelpText() {
    return helpText;
  }

  public void setHelpText(Text helpText) {
    this.helpText = helpText;
  }

  public Map<String, Double> getProbCorrects() {
    if (this.probCorrects == null) {
      this.probCorrects = new HashMap<String, Double>();
    }
    return probCorrects;
  }

  public Double getProbCorrectForStrategy(AnswerAggregationStrategy strategy) {
    if (this.probCorrects == null) {
      probCorrects = new HashMap<String, Double>();
    }
    return probCorrects.get(strategy.toString());
  }

  public void setProbCorrects(Map<String, Double> probCorrect) {
    this.probCorrects = probCorrect;
  }

  public void setProbCorrectForStrategy(AnswerAggregationStrategy strategy, Double probCorrect) {
    if (this.probCorrects == null) {
      this.probCorrects = new HashMap<String, Double>();
    }
    this.probCorrects.put(strategy.toString(), probCorrect);
  }
}
