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
 * (build: 2013-08-14 15:32:06 UTC)
 * on 2013-08-16 at 06:38:07 UTC 
 * Modify at your own risk.
 */

package us.quizz.www.crowdquiz.entities.quizz.model;

/**
 * Model definition for UserAnswerFeedback.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the . For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class UserAnswerFeedback extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String correctAnswer;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String difficulty;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean isCorrect;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Key key;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String mid;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer numCorrectAnswers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer numTotalAnswers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String quiz;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userAnswer;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userid;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getCorrectAnswer() {
    return correctAnswer;
  }

  /**
   * @param correctAnswer correctAnswer or {@code null} for none
   */
  public UserAnswerFeedback setCorrectAnswer(java.lang.String correctAnswer) {
    this.correctAnswer = correctAnswer;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDifficulty() {
    return difficulty;
  }

  /**
   * @param difficulty difficulty or {@code null} for none
   */
  public UserAnswerFeedback setDifficulty(java.lang.String difficulty) {
    this.difficulty = difficulty;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getIsCorrect() {
    return isCorrect;
  }

  /**
   * @param isCorrect isCorrect or {@code null} for none
   */
  public UserAnswerFeedback setIsCorrect(java.lang.Boolean isCorrect) {
    this.isCorrect = isCorrect;
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
  public UserAnswerFeedback setKey(Key key) {
    this.key = key;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getMid() {
    return mid;
  }

  /**
   * @param mid mid or {@code null} for none
   */
  public UserAnswerFeedback setMid(java.lang.String mid) {
    this.mid = mid;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getNumCorrectAnswers() {
    return numCorrectAnswers;
  }

  /**
   * @param numCorrectAnswers numCorrectAnswers or {@code null} for none
   */
  public UserAnswerFeedback setNumCorrectAnswers(java.lang.Integer numCorrectAnswers) {
    this.numCorrectAnswers = numCorrectAnswers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getNumTotalAnswers() {
    return numTotalAnswers;
  }

  /**
   * @param numTotalAnswers numTotalAnswers or {@code null} for none
   */
  public UserAnswerFeedback setNumTotalAnswers(java.lang.Integer numTotalAnswers) {
    this.numTotalAnswers = numTotalAnswers;
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
  public UserAnswerFeedback setQuiz(java.lang.String quiz) {
    this.quiz = quiz;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserAnswer() {
    return userAnswer;
  }

  /**
   * @param userAnswer userAnswer or {@code null} for none
   */
  public UserAnswerFeedback setUserAnswer(java.lang.String userAnswer) {
    this.userAnswer = userAnswer;
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
  public UserAnswerFeedback setUserid(java.lang.String userid) {
    this.userid = userid;
    return this;
  }

  @Override
  public UserAnswerFeedback set(String fieldName, Object value) {
    return (UserAnswerFeedback) super.set(fieldName, value);
  }

  @Override
  public UserAnswerFeedback clone() {
    return (UserAnswerFeedback) super.clone();
  }

}