package us.quizz.entities;

import com.google.common.base.Preconditions;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import us.quizz.enums.QuizKind;
import us.quizz.utils.Helper;

import java.io.Serializable;

// The Quiz is the basic unit of the application. Each quiz contains a set of
// Questions. The Quiz object is essentially a placeholder for storing overall
// statistics about the quiz, and for storing the id and the title of the quiz.
@Entity
@Cache
@Index
public class Quiz implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_NUM_CHOICES = 4;

  // The name of the quiz that we are targeting.
  @Id
  private String quizID;

  // The user-friendly name of the quiz that we are targeting
  private String name;

  // This defines the type of questions that can be entered into the quiz
  // e.g., can be either free-text or multiple choice. See the corresponding
  // enum for the currently supported set.
  private QuizKind kind;

  // This is only used for multiple choice tests and indicates the number of
  // answers that each multiple choice question should have
  private Integer numChoices;

  // The id of the AdWords ad campaign that brings visitors to the quiz
  private Long campaignid;

  // Whether to show this quizz on default on the Quizz landing page.
  private Boolean showOnDefault;

  // All the variables below are aggregate statistics about the quiz.
  // We update these using the QuizService.updateQuizCounts() call
  // The number of users that arrived in a Quiz page
  private Integer totalUsers;

  // The number of users that answered at least one non-IDK question
  private Integer contributingUsers;

  // The conversion rate = contributingUsers/totalUsers
  private Double conversionRate;

  // The number of correct answers submitted
  private Integer correctAnswers;

  // The total number of non-IDK answers submitted
  private Integer totalAnswers;

  // The total number of answers submitted for calibration questions
  private Integer totalCalibrationAnswers;

  // The total number of answers submitted for collection questions
  private Integer totalCollectionAnswers;

  // The total number of all answers submitted (calibration + collection).
  private Integer submitted;

  // The average correctness of the users
  private Double avgUserCorrectness;

  // The probability that a submitted answer is correct
  private Double avgAnswerCorrectness;

  // The average number of bits submitted by each user
  private Double capacity;

  // The number of questions for the quiz
  private Integer questions;

  // The number of questions for the quiz that have gold answers
  private Integer gold;

  // The quality of quiz's bestAnswer for all calibration questions (percentage of calibration
  // questions having the correct answer chosen as likelyAnswer) based on the BAYES_PROB
  // AnswerAggregationStrategy.
  // Ranges from [0, 1]. 1.0 means we did perfectly on the quiz's calibration answer.
  private Double bayesProbQuizQuality;

  // The quality of quiz's bestAnswer for all calibration questions (percentage of calibration
  // questions having the correct answer chosen as likelyAnswer) based on the MAJORITY_VOTE
  // AnswerAggregationStrategy. 
  // Ranges from [0, 1]. 1.0 means we did perfectly on the quiz's calibration answer.
  private Double majorityVoteProbQuizQuality;

  // The quality of quiz's bestAnswer for all calibration questions (percentage of calibration
  // questions having the correct answer chosen as likelyAnswer) based on the WEIGHTED_VOTE
  // AnswerAggregationStrategy. 
  // Ranges from [0, 1]. 1.0 means we did perfectly on the quiz's calibration answer.
  private Double weightedVoteProbQuizQuality;

  // Whether this quiz should use a questions selection strategy.
  private Boolean useQuestionSelectionStrategy;

  // Whether this quiz will allow varying number of questions in a single quiz (as opposed to
  // a fixed DEFAULT_NUM_QUESTIONS_PER_QUIZ questions).
  private Boolean allowVaryingLengthQuizSession;

  //for Objectify
  @SuppressWarnings("unused")
  private Quiz(){}

  public Quiz(String name, String quizID, QuizKind kind) {
    this.name = name;
    this.quizID = quizID;
    this.kind = kind;
    if (kind == QuizKind.MULTIPLE_CHOICE) {
      this.numChoices = DEFAULT_NUM_CHOICES;
    }
    this.showOnDefault = false;
    this.campaignid = null;

    this.totalUsers = 0;
    this.contributingUsers = 0;
    this.conversionRate = 0.0;
    this.correctAnswers = 0;
    this.totalAnswers = 0;
    this.totalCalibrationAnswers = 0;
    this.totalCollectionAnswers = 0;
    this.submitted = 0;
    this.avgUserCorrectness = 0.0;
    this.avgAnswerCorrectness = 0.0;
    this.capacity = 0.0;
    this.questions = 0;
    this.gold = 0;
    this.useQuestionSelectionStrategy = false;
    this.allowVaryingLengthQuizSession = false;
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

  public Boolean getShowOnDefault() {
    return showOnDefault;
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

  public void setShowOnDefault(Boolean showOnDefault) {
    Preconditions.checkNotNull(showOnDefault);
    this.showOnDefault = showOnDefault;
  }

  public Double getBayesProbQuizQuality() {
    return this.bayesProbQuizQuality;
  }

  public void setBayesProbQuizQuality(Double bayesProbQuizQuality) {
    this.bayesProbQuizQuality = bayesProbQuizQuality;
  }

  public Double getMajorityVoteProbQuizQuality() {
    return this.majorityVoteProbQuizQuality;
  }

  public void setMajorityVoteProbQuizQuality(Double majorityVoteProbQuizQuality) {
    this.majorityVoteProbQuizQuality = majorityVoteProbQuizQuality;
  }

  public Double getWeightedVoteProbQuizQuality() {
    return this.weightedVoteProbQuizQuality;
  }

  public void setWeightedVoteProbQuizQuality(Double weightedVoteProbQuizQuality) {
    this.weightedVoteProbQuizQuality = weightedVoteProbQuizQuality;
  }

  public Boolean getUseQuestionSelectionStrategy() {
    return this.useQuestionSelectionStrategy;
  }

  public void setUseQuestionSelectionStrategy(Boolean useQuestionSelectionStrategy) {
    Preconditions.checkNotNull(useQuestionSelectionStrategy,
        "Question selection strategy cannot be null.");
    this.useQuestionSelectionStrategy = useQuestionSelectionStrategy;
  }

  public Boolean getAllowVaryingLengthQuizSession() {
    return this.allowVaryingLengthQuizSession;
  }

  public void setAllowVaryingLengthQuizSession(Boolean allowVaryingLengthQuizSession) {
    Preconditions.checkNotNull(allowVaryingLengthQuizSession,
        "allowVaryingLengthQuizSession cannot be null.");
    this.allowVaryingLengthQuizSession = allowVaryingLengthQuizSession;
  }
}
