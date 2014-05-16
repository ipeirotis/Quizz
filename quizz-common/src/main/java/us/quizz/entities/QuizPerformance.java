package us.quizz.entities;

import com.google.common.base.Preconditions;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import us.quizz.utils.Helper;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of the performance of a user within a Quiz. This is a "caching"
 * object that aggregates the results from the underlying UserAnswer objects. In
 * this object, we keep track of the number of total and correct answers that a
 * given user submitted for the quiz, the "score" of the user (the Bayesian
 * Information Gain compared to random choice) and the relative rank of the user
 * compared to other users.
 *
 * The two key functions are the compute and computeRank. The first one is a
 * relatively lightweight function that goes through all the "UserAnswer"
 * objects for the user-quiz combination, and examine the number of correct and
 * incorrect answers, and the computes the user score. The computeRank performs
 * a comparison of the user scores against the scores of all the other users
 * that participated in the quiz, and computes the relative rank of the user
 * within the group.
 */
@Entity
@Cache
@Index
public class QuizPerformance implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String id;
  // The userid of the user
  private String userid;
  // The id of the quiz
  private String quiz;
  // The number of answers given by the user for all questions (calibration + collection).
  private Integer totalanswers;
  // The number of answers given by the user for golden questions (calibration).
  private Integer totalCalibrationAnswers;
  // The number of correct answers given by the user
  private Integer correctanswers;
  // The number of incorrect answers given by the user
  private Integer incorrectanswers;
  // The total information gain by this user. This is the total number of
  // answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain.
  private Double percentageCorrect;
  // The total information gain by this user. This is the total number of
  // answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain.
  private Double score;
  // The total information gain by this user. This is the total number of
  // answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain.
  private Double bayes_infogain;
  // The (frequentist) total information gain by this user. This is the total
  // number of answers given (excluding the "I do not know" answers)
  // multiplied with the Information Gain, computed in a frequentist way.
  private Double freq_infogain;
  // The Bayesian information gain by this user, computed in an LCB fashion.
  // This is the total number of answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain minus one standard deviation.
  private Double lcb_infogain;
  // The rank across the IG score
  private Integer rankScore;
  // The number of other users that participated in the same quiz
  private Integer totalUsers;

  //for Objectify
  @SuppressWarnings("unused")
  private QuizPerformance(){}

  public QuizPerformance(String quiz, String userid) {
    this.id = QuizPerformance.generateId(quiz, userid);
    this.userid = userid;
    this.quiz = quiz;
    this.totalanswers = 0;
    this.totalCalibrationAnswers = 0;
    this.correctanswers = 0;
    this.incorrectanswers = 0;
    this.score = 0.0;
  }

  public static String generateId(String quiz, String userid) {
    return userid + "_" + quiz;
  }

  public void computeCorrect(List<UserAnswer> results, List<Question> questions) {
    // questionID -> Question.
    Map<Long, Question> questionsMap = new HashMap<Long, Question>(); 
    for (final Question question : questions) {
      questionsMap.put(question.getId(), question);
    }

    // Sort UserAnswer result by increasing timestamp. This modifies results.
    Collections.sort(results, new Comparator<UserAnswer>() {
      public int compare(UserAnswer userAnswer1, UserAnswer userAnswer2) {
        return (int) (userAnswer1.getTimestamp() - userAnswer2.getTimestamp());
      }
    });

    int numCalibrationAnswers = 0;
    int numCorrectAnswers = 0;
    int numAnswers = 0;
    for (UserAnswer ua : results) {
      if (ua.getAction().equals("Submit")) {
        ++numAnswers;
      }
      if (!questionsMap.containsKey(ua.getQuestionID())) {
        continue;
      }

      // Only counts each question once, based on user's first answer.
      // TODO(chunhowt): Have a better way to take into account of answers to the same question.
      Question question = questionsMap.remove(ua.getQuestionID());

      if (!question.getHasGoldAnswer()) {
        // If the question is not a gold question, ignore.
        continue;
      }

      if (ua.getAction().equals("Submit")) {
        numCalibrationAnswers++;
      }

      if (ua.getIsCorrect()) {
        numCorrectAnswers++;
      }
    }
    setTotalanswers(numAnswers);
    setCorrectanswers(numCorrectAnswers);
    setTotalCalibrationAnswers(numCalibrationAnswers);
    setIncorrectanswers(numCalibrationAnswers - numCorrectAnswers);

    int numberOfMultipleChoiceOptions = 4;

    double meanInfoGainFrequentist = 0;
    double meanInfoGainBayes = 0;
    double varInfoGainBayes = 0;
    try {
      meanInfoGainFrequentist = Helper.getInformationGain(
          getPercentageCorrect(), numberOfMultipleChoiceOptions);
      meanInfoGainBayes = Helper.getBayesianMeanInformationGain(
          getCorrectanswers(),
          getTotalanswers() - getCorrectanswers(),
          numberOfMultipleChoiceOptions);
      varInfoGainBayes = Helper.getBayesianVarianceInformationGain(
          getCorrectanswers(),
          getTotalanswers() - getCorrectanswers(),
          numberOfMultipleChoiceOptions);
    } catch (Exception e) {
      e.printStackTrace();
    }

    setFreqInfoGain(getTotalanswers() * meanInfoGainFrequentist);
    setBayesInfoGain(getTotalanswers() * meanInfoGainBayes);
    double lcbInfoGain =
        getTotalanswers() * (meanInfoGainBayes - Math.sqrt(varInfoGainBayes));
    if (Double.isNaN(lcbInfoGain) || lcbInfoGain < 0) {
      setLcbInfoGain(0.0);
    } else {
      setLcbInfoGain(lcbInfoGain);
    }
  }

  public void computeRank(List<QuizPerformance> results) {
    this.totalUsers = results.size();
    int higherScore = 0;
    for (QuizPerformance qp : results) {
      if (qp.userid.equals(this.userid)) {
        continue;
      }
      if (qp.getScore() >= this.getScore()) {
        higherScore++;
      }
    }
    this.rankScore = higherScore + 1;
  }

  public String displayPercentageCorrect() {
    NumberFormat percentFormat = NumberFormat.getPercentInstance();
    percentFormat.setMaximumFractionDigits(0);
    return percentFormat.format(this.getPercentageCorrect());
  }

  public String displayRankScore() {
    if (this.getRankScore() == null ||
        this.getTotalUsers() == null ||
        this.getTotalUsers() == 0) {
      return "--";
    }

    NumberFormat percentFormat = NumberFormat.getPercentInstance();
    percentFormat.setMaximumFractionDigits(0);
    return percentFormat.format(1.0 * this.getRankScore() / this.getTotalUsers());
  }

  public String displayScore() {
    NumberFormat format = NumberFormat.getInstance();
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(0);
    return format.format(100 * this.getScore());
  }

  public Integer getCorrectanswers() {
    return correctanswers;
  }

  public Integer getIncorrectanswers() {
    return incorrectanswers;
  }

  public Integer getTotalCalibrationAnswers() {
    return totalCalibrationAnswers;
  }

  public Double getPercentageCorrect() {
    if (this.totalanswers != null &&
        this.correctanswers != null &&
        this.totalanswers > 0) {
      this.percentageCorrect =
          Math.round(100.0 * this.correctanswers / this.totalanswers) / 100.0;
    }
    else {
      this.percentageCorrect = 0.0;
    }
    return this.percentageCorrect;
  }

  public String getQuiz() {
    return quiz;
  }

  public Integer getRankScore() {
    return rankScore;
  }

  public Double getScore() {
    this.score = this.freq_infogain;
    if (this.score == null) {
      return 0.0;
    }
    return score;
  }

  public Integer getTotalanswers() {
    return totalanswers;
  }

  public Integer getTotalUsers() {
    return totalUsers;
  }

  public String getUserid() {
    return userid;
  }

  public void increaseCorrect() {
    this.correctanswers++;
  }

  public void increaseIncorrect() {
    if (this.incorrectanswers == null) {
      this.incorrectanswers = 0;
    }
    this.incorrectanswers++;
  }

  public void increaseTotal() {
    this.totalanswers++;
  }

  public void setCorrectanswers(Integer correctanswers) {
    Preconditions.checkNotNull(correctanswers);
    this.correctanswers = correctanswers;
  }

  public void setIncorrectanswers(Integer incorrectanswers) {
    Preconditions.checkNotNull(incorrectanswers);
    this.incorrectanswers = incorrectanswers;
  }

  public void setTotalCalibrationAnswers(Integer totalCalibrationAnswers) {
    Preconditions.checkNotNull(totalCalibrationAnswers);
    this.totalCalibrationAnswers = totalCalibrationAnswers;
  }

  public void setQuiz(String quiz) {
    Preconditions.checkNotNull(quiz);
    this.quiz = quiz;
  }

  public void setRankScore(Integer rankScore) {
    Preconditions.checkNotNull(rankScore);
    this.rankScore = rankScore;
  }

  public void setTotalanswers(Integer totalanswers) {
    Preconditions.checkNotNull(totalanswers);
    this.totalanswers = totalanswers;
  }

  public void setTotalUsers(Integer totalUsers) {
    Preconditions.checkNotNull(totalUsers);
    this.totalUsers = totalUsers;
  }

  public void setUserid(String userid) {
    Preconditions.checkNotNull(userid);
    this.userid = userid;
  }

  public void setFreqInfoGain(Double freqInfoGain) {
    Preconditions.checkNotNull(freqInfoGain);
    this.freq_infogain = freqInfoGain;
  }

  public void setBayesInfoGain(Double bayesInfoGain) {
    Preconditions.checkNotNull(bayesInfoGain);
    this.bayes_infogain = bayesInfoGain;
  }

  public void setLcbInfoGain(Double lcbInfoGain) {
    Preconditions.checkNotNull(lcbInfoGain);
    this.lcb_infogain = lcbInfoGain;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
