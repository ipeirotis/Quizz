package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import us.quizz.utils.Helper;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

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
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class QuizPerformance implements Serializable {
  private static final long serialVersionUID = 1L;

  public static Key generateKeyFromID(String quiz, String userid) {
    return KeyFactory.createKey(QuizPerformance.class.getSimpleName(),
        "id_" + userid + "_" + quiz);
  }

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  // The userid of the user
  @Persistent
  String userid;

  // The id of the quiz
  @Persistent
  String quiz;

  // The number of answers given by the user
  @Persistent
  Integer totalanswers;

  // The number of correct answers given by the user
  @Persistent
  Integer correctanswers;

  // The number of incorrect answers given by the user
  @Persistent
  Integer incorrectanswers;

  // The total information gain by this user. This is the total number of
  // answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain.
  @Persistent
  Double percentageCorrect;

  // The total information gain by this user. This is the total number of
  // answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain.
  @Persistent
  Double score;

  // The total information gain by this user. This is the total number of
  // answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain.
  @Persistent
  Double bayes_infogain;

  // The (frequentist) total information gain by this user. This is the total
  // number of answers given (excluding the "I do not know" answers)
  // multiplied with the Information Gain, computed in a frequentist way.
  @Persistent
  Double freq_infogain;

  // The Bayesian information gain by this user, computed in an LCB fashion.
  // This is the total number of answers given (excluding the "I do not know" answers)
  // multiplied with the Bayesian Information Gain minus one standard deviation.
  @Persistent
  Double lcb_infogain;

  // The rank across the IG score
  @Persistent
  Integer rankScore;

  // The number of other users that participated in the same quiz
  @Persistent
  Integer totalUsers;

  public QuizPerformance(String quiz, String userid) {
    this.key = QuizPerformance.generateKeyFromID(quiz, userid);
    this.userid = userid;
    this.quiz = quiz;
    this.totalanswers = 0;
    this.correctanswers = 0;
    this.incorrectanswers = 0;
    this.score = 0.0;
  }

  public void computeCorrect(List<UserAnswer> results, List<Question> questions) {
    // questionID -> Question.
    Map<Long, Question> questionsMap = new HashMap<Long, Question>(); 
    for (final Question question : questions) {
      questionsMap.put(question.getID(), question);
    }

    // Sort UserAnswer result by increasing timestamp. This modifies results.
    Collections.sort(results, new Comparator<UserAnswer>() {
      public int compare(UserAnswer userAnswer1, UserAnswer userAnswer2) {
        return (int) (userAnswer1.getTimestamp() - userAnswer2.getTimestamp());
      }
    });

    int c = 0;
    int t = 0;
    for (UserAnswer ua : results) {
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

      Boolean correct = ua.getIsCorrect();
      if (ua.getAction().equals("Submit")) {
        t++;
      }

      if (correct) {
        c++;
      }
    }
    this.correctanswers = c;
    this.totalanswers = t;
    this.incorrectanswers = t - c;

    int numberOfMultipleChoiceOptions = 4;

    double meanInfoGainFrequentist = 0;
    double meanInfoGainBayes = 0;
    double varInfoGainBayes = 0;
    try {
      meanInfoGainFrequentist = Helper.getInformationGain(
          getPercentageCorrect(), numberOfMultipleChoiceOptions);
      meanInfoGainBayes = Helper.getBayesianMeanInformationGain(
          this.correctanswers,
          this.totalanswers - this.correctanswers,
          numberOfMultipleChoiceOptions);
      varInfoGainBayes = Helper.getBayesianVarianceInformationGain(
          this.correctanswers,
          this.totalanswers - this.correctanswers,
          numberOfMultipleChoiceOptions);
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.freq_infogain = this.totalanswers * meanInfoGainFrequentist;
    this.bayes_infogain = this.totalanswers * meanInfoGainBayes;
    this.lcb_infogain = this.totalanswers
        * (meanInfoGainBayes - Math.sqrt(varInfoGainBayes));
    if (Double.isNaN(this.lcb_infogain) || this.lcb_infogain < 0) {
      this.lcb_infogain = 0.0;
    }
  }

  public void computeRank(List<QuizPerformance> results) {
    this.totalUsers = results.size();
    int higherScore = 0;
    for (QuizPerformance qp : results) {
      if (qp.userid.equals(this.userid)) {
        continue;
      }
      if (qp.getScore() > this.getScore()) {
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

  public Key getKey() {
    return key;
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
    this.correctanswers = correctanswers;
  }

  public void setIncorrectanswers(Integer incorrectanswers) {
    this.incorrectanswers = incorrectanswers;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public void setQuiz(String quiz) {
    this.quiz = quiz;
  }

  public void setRankScore(Integer rankScore) {
    this.rankScore = rankScore;
  }

  public void setTotalanswers(Integer totalanswers) {
    this.totalanswers = totalanswers;
  }

  public void setTotalUsers(Integer totalUsers) {
    this.totalUsers = totalUsers;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }
}
