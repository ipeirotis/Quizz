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
 * Model definition for QuizQuestionInstance.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the . For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class QuizQuestionInstance extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.String> answers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String correct;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean correctIsGold;

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
  private java.lang.String mid;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String midname;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String quiz;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String quizquestion;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer totalanswers;

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getAnswers() {
    return answers;
  }

  /**
   * @param answers answers or {@code null} for none
   */
  public QuizQuestionInstance setAnswers(java.util.List<java.lang.String> answers) {
    this.answers = answers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getCorrect() {
    return correct;
  }

  /**
   * @param correct correct or {@code null} for none
   */
  public QuizQuestionInstance setCorrect(java.lang.String correct) {
    this.correct = correct;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getCorrectIsGold() {
    return correctIsGold;
  }

  /**
   * @param correctIsGold correctIsGold or {@code null} for none
   */
  public QuizQuestionInstance setCorrectIsGold(java.lang.Boolean correctIsGold) {
    this.correctIsGold = correctIsGold;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getCorrectanswers() {
    return correctanswers;
  }

  /**
   * @param correctanswers correctanswers or {@code null} for none
   */
  public QuizQuestionInstance setCorrectanswers(java.lang.Integer correctanswers) {
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
  public QuizQuestionInstance setKey(Key key) {
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
  public QuizQuestionInstance setMid(java.lang.String mid) {
    this.mid = mid;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getMidname() {
    return midname;
  }

  /**
   * @param midname midname or {@code null} for none
   */
  public QuizQuestionInstance setMidname(java.lang.String midname) {
    this.midname = midname;
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
  public QuizQuestionInstance setQuiz(java.lang.String quiz) {
    this.quiz = quiz;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getQuizquestion() {
    return quizquestion;
  }

  /**
   * @param quizquestion quizquestion or {@code null} for none
   */
  public QuizQuestionInstance setQuizquestion(java.lang.String quizquestion) {
    this.quizquestion = quizquestion;
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
  public QuizQuestionInstance setTotalanswers(java.lang.Integer totalanswers) {
    this.totalanswers = totalanswers;
    return this;
  }

  @Override
  public QuizQuestionInstance set(String fieldName, Object value) {
    return (QuizQuestionInstance) super.set(fieldName, value);
  }

  @Override
  public QuizQuestionInstance clone() {
    return (QuizQuestionInstance) super.clone();
  }

}