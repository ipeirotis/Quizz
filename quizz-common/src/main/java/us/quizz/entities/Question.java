package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

import us.quizz.enums.AnswerKind;
import us.quizz.enums.QuestionKind;
import us.quizz.enums.QuizKind;

import java.io.Serializable;
import java.util.ArrayList;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Question implements Serializable {
  private static final long serialVersionUID = 1L;
  
  // The quizID of the parent quiz
  @Persistent
  private String quizID;

  // The text of the question. Can be any HTML-compliant code
  // Use the questionText instead
  @Deprecated
  @Persistent
  private String text;

  // The text of the question. Can be any HTML-compliant code
  @Persistent
  private Text questionText;

  // The id assigned by the client/source for this question to allow us to rejoin the
  // question with the original source.
  @Persistent
  private String clientID;
  
  // The type of the question. Should match the type of the quiz that is added to
  @Persistent
  private QuestionKind kind;

  // Computed statistic about the number of users that answered this question
  @Persistent
  private Integer numberOfUserAnswers;

  // Computed statistic on whether the question has any user answers
  @Persistent
  private Boolean hasUserAnswers;
  
  // Computed statistic on  how many users answered this question correctly.
  // Applicable only for questions with GOLD 
  @Persistent
  private Integer numberOfCorrentUserAnswers;

  // Computed statistic showing the the total number of user bits assigned
  // to this question by the users that answered this question
  // Used to prioritize exposure of questions to users, favoring questions with low score
  @Persistent
  private Double totalUserScore;

  // Whether any of the answers of the question is GOLD. Should be updated
  // after adding the answers
  @Persistent
  private Boolean hasGoldAnswer;

  // Whether any of the answers of the question is SILVER. Should be updated
  // after adding the answers
  @Persistent
  private Boolean hasSilverAnswers;
  
  // After computing the probability of correctness for each answer, the
  // confidence is the highest probability 
  @Persistent
  private Double confidence;
  
  // After computing the probability of correctness for each answer,
  // this is the answer with the highest probability 
  @Persistent
  private String likelyAnswer;
  
  // If likelyAnswer matches a GOLD answer, we set this to true
  @Persistent
  private Boolean isLikelyAnswerCorrect;
  
  // The feedback that we give to the user to explain why a particular answer 
  // was correct (or incorrect)
  @Persistent
  private Text feedback;

  public Text getFeedback() {
    return feedback;
  }

  public void setFeedback(Text feedback) {
    this.feedback = feedback;
  }

  public Boolean getIsLikelyAnswerCorrect() {
    return isLikelyAnswerCorrect;
  }

  public void setIsLikelyAnswerCorrect(Boolean isLikelyAnswerCorrect) {
    this.isLikelyAnswerCorrect = isLikelyAnswerCorrect;
  }

  public Double getConfidence() {
    return confidence;
  }

  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  public String getLikelyAnswer() {
    return likelyAnswer;
  }

  public void setLikelyAnswer(String likelyAnswer) {
    this.likelyAnswer = likelyAnswer;
  }

  @Persistent(defaultFetchGroup = "true")
  private ArrayList<Answer> answers;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;
  
  public Question(String quizID, String text, QuestionKind kind) {
    this.quizID = quizID;
    this.text = text;
    this.kind = kind;
    this.hasSilverAnswers = false;
    this.hasGoldAnswer = false;
    this.numberOfUserAnswers = 0;
    this.hasUserAnswers = false;
    this.totalUserScore = 0.0;
    this.numberOfCorrentUserAnswers = 0;
    
    this.answers = new ArrayList<Answer>();
  }

  public Question(String quizID, Text questionText, QuestionKind kind) {
    this.quizID = quizID;
    this.questionText = questionText;
    this.kind = kind;
    this.hasSilverAnswers = false;
    this.hasGoldAnswer = false;
    this.numberOfUserAnswers = 0;
    this.hasUserAnswers = false;
    this.totalUserScore = 0.0;
    this.numberOfCorrentUserAnswers = 0;
    
    this.answers = new ArrayList<Answer>();
  }
  
  

  public Question(String quizID, String text, QuestionKind kind, Long questionID, String clientID,
                  Boolean hasGoldAnswer, Boolean hasSilverAnswers) {
    this(quizID, text, kind);
    this.clientID = clientID;
    this.hasGoldAnswer = hasGoldAnswer;
    this.hasSilverAnswers = hasSilverAnswers;
    this.key = Question.generateKeyFromID(questionID);
  }

  public static Key generateKeyFromID(Long questionID) {
    return KeyFactory.createKey(Question.class.getSimpleName(), questionID);
  }

  public String getQuizID() {
    return quizID;
  }

  public void setQuizID(String quizID) {
    this.quizID = quizID;
  }

  public String getClientID() {
    return clientID;
  }

  public void setClientID(String clientID) {
    this.clientID = clientID;
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

  public Key getKey() {
    return key;
  }

  public Long getID() {
    return key.getId();
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Text getQuestionText() {
    return questionText;
  }

  public void setQuestionText(Text questionText) {
    this.questionText = questionText;
  }

  public QuestionKind getKind() {
    return kind;
  }

  public void setKind(QuestionKind kind) {
    this.kind = kind;
  }

  public void setRelation(String quizID) {
    this.quizID = quizID;
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
    try {
      Answer a = answers.get(answerID);
      return a;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
    
  }
}
