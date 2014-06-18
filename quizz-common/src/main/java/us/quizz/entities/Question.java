package us.quizz.entities;

import com.google.appengine.api.datastore.Text;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import us.quizz.enums.QuestionKind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;

@Entity
@Cache
@Index
public class Question implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(Question.class.getName());

  @Id
  private Long id;
  // The quizID of the parent quiz
  private String quizID;

  // The instruction of the question. Can be any HTML-compliant code.
  private Text instruction;

  // The text of the question. Can be any HTML-compliant code
  private Text questionText;

  // The id assigned by the client/source for this question to allow us to rejoin the
  // question with the original source.
  private String clientID;

  // The type of the question. Should match the type of the quiz that is added to
  private QuestionKind kind;

  // Computed statistic about the number of users that answered this question
  private Integer numberOfUserAnswers;

  // Computed statistic on whether the question has any user answers
  private Boolean hasUserAnswers;

  // Computed statistic on how many users answered this question correctly.
  // Applicable only for calibration questions. 
  private Integer numberOfCorrentUserAnswers;

  // Computed statistic showing the the total number of user bits assigned
  // to this question by the users that answered this question
  // Used to prioritize exposure of questions to users, favoring questions with low score
  private Double totalUserScore;

  // Whether any of the answers of the question is GOLD. Should be updated
  // after adding the answers
  private Boolean hasGoldAnswer;

  // Whether any of the answers of the question is SILVER. Should be updated
  // after adding the answers
  private Boolean hasSilverAnswers;

  // After computing the probability of correctness for each answer, the
  // confidence is the highest probability 
  private Double confidence;

  // After computing the probability of correctness for each answer,
  // this is the answer with the highest probability 
  private String likelyAnswer;

  // ID of most likely answer
  private Integer likelyAnswerID;

  // If likelyAnswer matches a GOLD answer, we set this to true
  private Boolean isLikelyAnswerCorrect;

  // The feedback that we give to the user to explain why a particular answer 
  // was correct (or incorrect)
  private String feedback;

  private ArrayList<Answer> answers;

  // The prior difficulty of a question between [0, 1] (0 is easiest); this is computed offline
  private Double difficultyPrior;

  // The difficulty of this question between [0, 1] (0 is easiest); this is computed online
  private Double difficulty;

  // Answer ID of best answer using BAYES_PROB answer aggregation strategy.
  private Integer bestBayesProbAnswerID;

  // Answer ID of best answer using MAJORITY_VOTE answer aggregation strategy.
  private Integer bestMajorityVoteProbAnswerID;

  // Answer ID of best answer using WEIGHTED_VOTE answer aggregation strategy.
  private Integer bestWeightedVoteProbAnswerID;

  //for Objectify
  @SuppressWarnings("unused")
  private Question(){}

  public Question(String quizID, Text questionText, QuestionKind kind) {
    this.quizID = quizID;
    this.questionText = questionText;
    this.kind = kind;
    this.numberOfUserAnswers = 0;
    this.hasUserAnswers = false;
    this.numberOfCorrentUserAnswers = 0;
    this.totalUserScore = 0.0;
    this.hasGoldAnswer = false;
    this.hasSilverAnswers = false;
    this.confidence = 0.0;
    this.likelyAnswer = "";
    this.isLikelyAnswerCorrect = false;
    this.feedback = "";
    this.difficultyPrior = 0.5;  // 0.5 is "half" difficult
    this.difficulty = 0.0;

    this.answers = new ArrayList<Answer>();
  }

  // Note: This function should ONLY be used for test purpose because it sets the questionID
  // explicitly.
  // TODO(chunhowt): Makes this a private/protected method only visible for testing.
  public Question(String quizID, Text text, QuestionKind kind, Long questionID, String clientID,
                  Boolean hasGoldAnswer, Boolean hasSilverAnswers, Double totalUserScore) {
    this(quizID, text, kind);
    logger.warning("This Question constructor should only be used for testing.");
    // Note: We are setting the entity id here explicitly so that we can control it for unit
    // test purpose.
    this.id = questionID;
    this.clientID = clientID;
    this.hasGoldAnswer = hasGoldAnswer;
    this.hasSilverAnswers = hasSilverAnswers;
    this.totalUserScore = totalUserScore;
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

  public Text getInstruction() {
    return instruction;
  }

  public void setInstruction(Text instruction) {
    this.instruction = instruction;
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

  public Boolean hasUserAnswers() {
    return numberOfUserAnswers > 0;
  }

  public Integer getNumberOfUserAnswers() {
    return numberOfUserAnswers;
  }

  public void setNumberOfUserAnswers(Integer numberOfUserAnswers) {
    this.numberOfUserAnswers = numberOfUserAnswers;
  }

  public Integer getNumberOfCorrectUserAnswers() {
    return numberOfCorrentUserAnswers;
  }

  public void setNumberOfCorrectUserAnswers(Integer numberOfCorrectUserAnswers) {
    this.numberOfCorrentUserAnswers = numberOfCorrectUserAnswers;
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
    this.answers.add(answer);
  }

  public Answer getAnswer(Integer answerID) {
    try {
      return answers.get(answerID);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
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

  public Double getDifficultyPrior() {
    return difficultyPrior;
  }

  public void setDifficultyPrior(Double difficultyPrior) {
    this.difficultyPrior = difficultyPrior;
  }

  public Double getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(Double difficulty) {
    this.difficulty = difficulty;
  }

  public Integer getLikelyAnswerID() {
    return likelyAnswerID;
  }

  public void setLikelyAnswerID(Integer likelyAnswerID) {
    this.likelyAnswerID = likelyAnswerID;
  }

  public Integer getBestBayesProbAnswerID() {
    return bestBayesProbAnswerID;
  }

  public void setBestBayesProbAnswerID(Integer bestAnswerID) {
    this.bestBayesProbAnswerID = bestAnswerID;
  }

  public Integer getBestWeightedVoteProbAnswerID() {
    return bestWeightedVoteProbAnswerID;
  }

  public void setBestWeightedVoteProbAnswerID(Integer bestAnswerID) {
    this.bestWeightedVoteProbAnswerID = bestAnswerID;
  }

  public Integer getBestMajorityVoteProbAnswerID() {
    return bestMajorityVoteProbAnswerID;
  }

  public void setBestMajorityVoteProbAnswerID(Integer bestAnswerID) {
    this.bestMajorityVoteProbAnswerID = bestAnswerID;
  }
}
