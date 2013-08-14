/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2013-08-07 19:00:49 UTC)
 * on 2013-08-14 at 07:23:39 UTC 
 * Modify at your own risk.
 */

package us.quizz.www.crowdquiz.entities.quizz.model;

/**
 * Model definition for QuizPerformance.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the . For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class QuizPerformance extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer correctanswers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Key key;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double percentageCorrect;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String quiz;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer rankPercentCorrect;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer rankScore;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer rankTotalCorrect;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double score;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer totalUsers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer totalanswers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userid;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getCorrectanswers() {
    return correctanswers;
  }

  /**
   * @param correctanswers correctanswers or {@code null} for none
   */
  public QuizPerformance setCorrectanswers(java.lang.Integer correctanswers) {
    this.correctanswers = correctanswers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public Key getKey() {
    return key;
  }

  /**
   * @param key key or {@code null} for none
   */
  public QuizPerformance setKey(Key key) {
    this.key = key;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getPercentageCorrect() {
    return percentageCorrect;
  }

  /**
   * @param percentageCorrect percentageCorrect or {@code null} for none
   */
  public QuizPerformance setPercentageCorrect(java.lang.Double percentageCorrect) {
    this.percentageCorrect = percentageCorrect;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getQuiz() {
    return quiz;
  }

  /**
   * @param quiz quiz or {@code null} for none
   */
  public QuizPerformance setQuiz(java.lang.String quiz) {
    this.quiz = quiz;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getRankPercentCorrect() {
    return rankPercentCorrect;
  }

  /**
   * @param rankPercentCorrect rankPercentCorrect or {@code null} for none
   */
  public QuizPerformance setRankPercentCorrect(java.lang.Integer rankPercentCorrect) {
    this.rankPercentCorrect = rankPercentCorrect;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getRankScore() {
    return rankScore;
  }

  /**
   * @param rankScore rankScore or {@code null} for none
   */
  public QuizPerformance setRankScore(java.lang.Integer rankScore) {
    this.rankScore = rankScore;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getRankTotalCorrect() {
    return rankTotalCorrect;
  }

  /**
   * @param rankTotalCorrect rankTotalCorrect or {@code null} for none
   */
  public QuizPerformance setRankTotalCorrect(java.lang.Integer rankTotalCorrect) {
    this.rankTotalCorrect = rankTotalCorrect;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getScore() {
    return score;
  }

  /**
   * @param score score or {@code null} for none
   */
  public QuizPerformance setScore(java.lang.Double score) {
    this.score = score;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getTotalUsers() {
    return totalUsers;
  }

  /**
   * @param totalUsers totalUsers or {@code null} for none
   */
  public QuizPerformance setTotalUsers(java.lang.Integer totalUsers) {
    this.totalUsers = totalUsers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getTotalanswers() {
    return totalanswers;
  }

  /**
   * @param totalanswers totalanswers or {@code null} for none
   */
  public QuizPerformance setTotalanswers(java.lang.Integer totalanswers) {
    this.totalanswers = totalanswers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserid() {
    return userid;
  }

  /**
   * @param userid userid or {@code null} for none
   */
  public QuizPerformance setUserid(java.lang.String userid) {
    this.userid = userid;
    return this;
  }

  @Override
  public QuizPerformance set(String fieldName, Object value) {
    return (QuizPerformance) super.set(fieldName, value);
  }

  @Override
  public QuizPerformance clone() {
    return (QuizPerformance) super.clone();
  }

}
