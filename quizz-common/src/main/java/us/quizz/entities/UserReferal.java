package us.quizz.entities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import nl.bitwalker.useragentutils.Browser;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserReferal implements Serializable {
  private static final long serialVersionUID = 1L;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  // The id for the user.
  @Persistent
  private String userid;

  // The id for the user.
  @Persistent
  private String quiz;

  @Persistent
  private Long timestamp;

  @Persistent
  private String ipaddress;

  @Persistent
  private Text referer;

  @Persistent
  private String domain;

  @Persistent
  private Browser browser;

  public UserReferal(String userid) {
    this.userid = userid;
    this.timestamp = (new Date()).getTime();
  }

  /**
   * @return the key
   */
  public Key getKey() {
    return key;
  }

  /**
   * @param key The key to set
   */
  public void setKey(Key key) {
    this.key = key;
  }

  /**
   * @return the quiz
   */
  public String getQuiz() {
    return quiz;
  }

  /**
   * @param quiz the quiz to set
   */
  public void setQuiz(String quiz) {
    this.quiz = quiz;
  }

  /**
   * @return the ipaddress
   */
  public String getIpaddress() {
    return ipaddress;
  }

  /**
   * @param ipaddress the ipaddress to set
   */
  public void setIpaddress(String ipaddress) {
    // anonymizing the last 4 digits
    ipaddress = ipaddress.substring(0, ipaddress.length() - 4);
    this.ipaddress = ipaddress + "XXXX";
  }

  /**
   * @return the referer
   */
  public Text getReferer() {
    return referer;
  }

  /**
   * @param referer the referer to set
   */
  public void setReferer(String referer) {
    if (referer != null) {
      this.referer = new Text(referer);
    } else {
      this.referer = null;
    }
  }

  /**
   * @return the userid
   */
  public String getUserid() {
    return userid;
  }

  /**
   * @return the timestamp
   */
  public Long getTimestamp() {
    return timestamp;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * @return the browser
   */
  public Browser getBrowser() {
    return browser;
  }

  /**
   * @param browser the browser to set
   */
  public void setBrowser(Browser browser) {
    this.browser = browser;
  }
}
