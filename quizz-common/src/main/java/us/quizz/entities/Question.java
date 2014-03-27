package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import us.quizz.enums.AnswerKind;
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
  @Persistent
  private String text;
  
  // The type of the question. Should match the type of the quiz that is added to
  @Persistent
  private QuizKind kind;

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
  // Can be used (although not used currently) to prioritize exposure of
  // questions to users, favoring questions with low score
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


  @Persistent(defaultFetchGroup = "true")
  private ArrayList<Answer> answers;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;
  
  public Question(String quizID, String text, QuizKind kind) {
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
  
  public static Key generateKeyFromID(Long id) {
    return KeyFactory.createKey(Question.class.getSimpleName(), id);
  }

  public String getQuizID() {
    return quizID;
  }

  public void setQuizID(String quizID) {
    this.quizID = quizID;
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
  
  public QuizKind getKind() {
    return kind;
  }

  public void setKind(QuizKind kind) {
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
