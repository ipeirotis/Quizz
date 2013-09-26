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
 * (build: 2013-09-16 16:01:30 UTC)
 * on 2013-09-26 at 20:32:54 UTC 
 * Modify at your own risk.
 */

package us.quizz.www.crowdquiz.entities.quizz.model;

/**
 * Model definition for Quiz.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the quizz. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Quiz extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double avgUserCorrectness;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long campaignid;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double capacity;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer contributingUsers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double conversionRate;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer correctAnswers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String freebaseType;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer gold;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Key key;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String name;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String questionText;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer questions;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String relation;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer silver;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer submitted;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer totalAnswers;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer totalUsers;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getAvgUserCorrectness() {
    return avgUserCorrectness;
  }

  /**
   * @param avgUserCorrectness avgUserCorrectness or {@code null} for none
   */
  public Quiz setAvgUserCorrectness(java.lang.Double avgUserCorrectness) {
    this.avgUserCorrectness = avgUserCorrectness;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getCampaignid() {
    return campaignid;
  }

  /**
   * @param campaignid campaignid or {@code null} for none
   */
  public Quiz setCampaignid(java.lang.Long campaignid) {
    this.campaignid = campaignid;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getCapacity() {
    return capacity;
  }

  /**
   * @param capacity capacity or {@code null} for none
   */
  public Quiz setCapacity(java.lang.Double capacity) {
    this.capacity = capacity;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getContributingUsers() {
    return contributingUsers;
  }

  /**
   * @param contributingUsers contributingUsers or {@code null} for none
   */
  public Quiz setContributingUsers(java.lang.Integer contributingUsers) {
    this.contributingUsers = contributingUsers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getConversionRate() {
    return conversionRate;
  }

  /**
   * @param conversionRate conversionRate or {@code null} for none
   */
  public Quiz setConversionRate(java.lang.Double conversionRate) {
    this.conversionRate = conversionRate;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getCorrectAnswers() {
    return correctAnswers;
  }

  /**
   * @param correctAnswers correctAnswers or {@code null} for none
   */
  public Quiz setCorrectAnswers(java.lang.Integer correctAnswers) {
    this.correctAnswers = correctAnswers;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getFreebaseType() {
    return freebaseType;
  }

  /**
   * @param freebaseType freebaseType or {@code null} for none
   */
  public Quiz setFreebaseType(java.lang.String freebaseType) {
    this.freebaseType = freebaseType;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getGold() {
    return gold;
  }

  /**
   * @param gold gold or {@code null} for none
   */
  public Quiz setGold(java.lang.Integer gold) {
    this.gold = gold;
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
  public Quiz setKey(Key key) {
    this.key = key;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getName() {
    return name;
  }

  /**
   * @param name name or {@code null} for none
   */
  public Quiz setName(java.lang.String name) {
    this.name = name;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getQuestionText() {
    return questionText;
  }

  /**
   * @param questionText questionText or {@code null} for none
   */
  public Quiz setQuestionText(java.lang.String questionText) {
    this.questionText = questionText;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getQuestions() {
    return questions;
  }

  /**
   * @param questions questions or {@code null} for none
   */
  public Quiz setQuestions(java.lang.Integer questions) {
    this.questions = questions;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getRelation() {
    return relation;
  }

  /**
   * @param relation relation or {@code null} for none
   */
  public Quiz setRelation(java.lang.String relation) {
    this.relation = relation;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getSilver() {
    return silver;
  }

  /**
   * @param silver silver or {@code null} for none
   */
  public Quiz setSilver(java.lang.Integer silver) {
    this.silver = silver;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getSubmitted() {
    return submitted;
  }

  /**
   * @param submitted submitted or {@code null} for none
   */
  public Quiz setSubmitted(java.lang.Integer submitted) {
    this.submitted = submitted;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getTotalAnswers() {
    return totalAnswers;
  }

  /**
   * @param totalAnswers totalAnswers or {@code null} for none
   */
  public Quiz setTotalAnswers(java.lang.Integer totalAnswers) {
    this.totalAnswers = totalAnswers;
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
  public Quiz setTotalUsers(java.lang.Integer totalUsers) {
    this.totalUsers = totalUsers;
    return this;
  }

  @Override
  public Quiz set(String fieldName, Object value) {
    return (Quiz) super.set(fieldName, value);
  }

  @Override
  public Quiz clone() {
    return (Quiz) super.clone();
  }

}
