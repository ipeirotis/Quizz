package us.quizz.entities;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import us.quizz.enums.QuizKind;
import us.quizz.utils.Helper;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * The Quiz is the basic unit of the application. Each quiz contains a set of
 * Questions. The Quiz object is essentially a placeholder for storing overall
 * statistics about the quiz, and for storing the id and the title of the quiz.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Quiz implements Serializable {
  private static final long serialVersionUID = 1L;

  public static Key generateKeyFromID(String quizID) {
    return KeyFactory.createKey(Quiz.class.getSimpleName(), "id_" + quizID);
  }

  // The user-friendly name of the quiz that we are targeting
  @Persistent
  private String name;

  // The name of the quiz that we are targeting.
  // Typically, we assign the name of a KP attribute on this one
  // and serves as a defacto primary key for the quiz.
  @Persistent
  private String quizID;
  
 
  // This defines the type of questions that can be entered into the quiz
  // e.g., can be either free-text or multiple choice. See the corresponding
  // enum for the currently supported set.
  @Persistent
  private QuizKind kind;
  
  // This is only used for multiple choice tests and indicates the number of
  // answers that each multiple choice question should have
  @Persistent
  private Integer numChoices;

  // The id of the AdWords ad campaign that brings visitors to the quiz
  @Persistent
  private Long campaignid;

  // All the variables below are aggregate statistics about the quiz.
  // We update these using the QuizRepository.updateQuizCounts() call

  // The number of users that arrived in a Quiz page
  @Persistent
  private Integer totalUsers;

  // The number of users that answered at least one non-IDK question
  @Persistent
  private Integer contributingUsers;

  // The conversion rate = contributingUsers/totalUsers
  @Persistent
  private Double conversionRate;

  // The number of correct answers submitted
  @Persistent
  private Integer correctAnswers;

  // The total number of non-IDK answers submitted
  @Persistent
  private Integer totalAnswers;

  // The total number of answers submitted for calibration questions
  @Persistent
  private Integer totalCalibrationAnswers;

  // The total number of answers submitted for collection questions
  @Persistent
  private Integer totalCollectionAnswers;

  // The total number of all answers submitted (calibration + collection).
  @Persistent
  private Integer submitted;

  // The average correctness of the users
  @Persistent
  private Double avgUserCorrectness;

  // The probability that a submitted answer is correct
  @Persistent
  private Double avgAnswerCorrectness;

  // The average number of bits submitted by each user
  @Persistent
  private Double capacity;

  // The number of questions for the quiz
  @Persistent
  private Integer questions;

  // The number of questions for the quiz that have gold answers
  @Persistent
  private Integer gold;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  public Quiz(String name, String quizID, QuizKind kind) {
    this.name = name;
    this.quizID = quizID;
    this.kind = kind;
    this.key = generateKeyFromID(quizID);
  }

  public Double getAvgAnswerCorrectness() {
    return avgAnswerCorrectness;
  }

  public Double getAvgUserCorrectness() {
    return avgUserCorrectness;
  }

  public Long getCampaignid() {
    return campaignid;
  }

  public Double getCapacity() {
    return capacity;
  }

  public Double getCapacity(Double error) {
    try {
      return capacity / (1 - Helper.entropy(1.0 - error, 2));
    } catch (Exception e) {
      e.printStackTrace();
      return capacity;
    }
  }

  public Integer getContributingUsers() {
    return contributingUsers;
  }

  public Double getConversionRate() {
    return conversionRate;
  }

  public Integer getCorrectAnswers() {
    return correctAnswers;
  }

  public Integer getGold() {
    return gold;
  }

  public Key getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public Integer getQuestions() {
    return questions;
  }

  public String getQuizID() {
    return quizID;
  }

  public Integer getSubmitted() {
    return submitted;
  }

  public Integer getTotalCalibrationAnswers() {
    return totalCalibrationAnswers;
  }

  public Integer getTotalCollectionAnswers() {
    return totalCollectionAnswers;
  }

  public Integer getTotalAnswers() {
    return totalAnswers;
  }

  public Integer getTotalUsers() {
    return totalUsers;
  }

  public void setAvgAnswerCorrectness(Double avgAnswerCorrectness) {
    Preconditions.checkNotNull(avgAnswerCorrectness);
    this.avgAnswerCorrectness = avgAnswerCorrectness;
  }

  public void setAvgUserCorrectness(Double avgUserCorrectness) {
    Preconditions.checkNotNull(avgUserCorrectness);
    this.avgUserCorrectness = avgUserCorrectness;
  }

  public void setCampaignid(Long campaignid) {
    Preconditions.checkNotNull(campaignid);
    this.campaignid = campaignid;
  }

  public void setCapacity(Double capacity) {
    Preconditions.checkNotNull(capacity);
    this.capacity = capacity;
  }

  public void setContributingUsers(Integer contributingUsers) {
    Preconditions.checkNotNull(contributingUsers);
    this.contributingUsers = contributingUsers;
  }

  public void setConversionRate(Double conversionRate) {
    Preconditions.checkNotNull(conversionRate);
    this.conversionRate = conversionRate;
  }

  public void setCorrectAnswers(Integer correctAnswers) {
    Preconditions.checkNotNull(correctAnswers);
    this.correctAnswers = correctAnswers;
  }

  public void setGold(Integer gold) {
    Preconditions.checkNotNull(gold);
    this.gold = gold;
  }

  public void setKey(Key key) {
    Preconditions.checkNotNull(key);
    this.key = key;
  }

  public void setName(String name) {
    Preconditions.checkNotNull(name);
    this.name = name;
  }

  public void setQuestions(Integer questions) {
    Preconditions.checkNotNull(questions);
    this.questions = questions;
  }

  public void setQuizID(String quizID) {
    Preconditions.checkNotNull(quizID);
    this.quizID = quizID;
  }

  public void setSubmitted(Integer submitted) {
    Preconditions.checkNotNull(submitted);
    this.submitted = submitted;
  }

  public void setTotalCalibrationAnswers(Integer totalCalibrationAnswers) {
    Preconditions.checkNotNull(totalCalibrationAnswers);
    this.totalCalibrationAnswers = totalCalibrationAnswers;
  }

  public void setTotalCollectionAnswers(Integer totalCollectionAnswers) {
    Preconditions.checkNotNull(totalCollectionAnswers);
    this.totalCollectionAnswers = totalCollectionAnswers;
  }

  public void setTotalAnswers(Integer totalAnswers) {
    Preconditions.checkNotNull(totalAnswers);
    this.totalAnswers = totalAnswers;
  }

  public void setTotalUsers(Integer totalUsers) {
    Preconditions.checkNotNull(totalUsers);
    this.totalUsers = totalUsers;
  }

  public QuizKind getKind() {
    return kind;
  }

  public void setKind(QuizKind kind) {
    Preconditions.checkNotNull(kind);
    this.kind = kind;
  }
  
  public Integer getNumChoices() {
    return numChoices;
  }

  public void setNumChoices(Integer numChoices) {
    Preconditions.checkNotNull(numChoices);
    this.numChoices = numChoices;
  }
}
