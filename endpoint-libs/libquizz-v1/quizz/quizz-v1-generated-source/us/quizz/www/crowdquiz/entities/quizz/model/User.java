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
 * Model definition for User.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the quizz. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class User extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Experiment experiment;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Key key;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private JsonMap treatments;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userid;

  /**
   * @return value or {@code null} for none
   */
  public Experiment getExperiment() {
    return experiment;
  }

  /**
   * @param experiment experiment or {@code null} for none
   */
  public User setExperiment(Experiment experiment) {
    this.experiment = experiment;
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
  public User setKey(Key key) {
    this.key = key;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public JsonMap getTreatments() {
    return treatments;
  }

  /**
   * @param treatments treatments or {@code null} for none
   */
  public User setTreatments(JsonMap treatments) {
    this.treatments = treatments;
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
  public User setUserid(java.lang.String userid) {
    this.userid = userid;
    return this;
  }

  @Override
  public User set(String fieldName, Object value) {
    return (User) super.set(fieldName, value);
  }

  @Override
  public User clone() {
    return (User) super.clone();
  }

}
