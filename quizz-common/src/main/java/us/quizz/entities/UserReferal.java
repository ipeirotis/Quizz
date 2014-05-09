package us.quizz.entities;

import java.io.Serializable;
import java.util.Date;

import nl.bitwalker.useragentutils.Browser;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
@Index
public class UserReferal implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private Long id;
  private String userid;
  private String quiz;
  private Long timestamp;
  private String ipaddress;
  private Text referer;
  private String domain;
  private Browser browser;
  
  //for Objectify
  @SuppressWarnings("unused")
  private UserReferal(){}

  public UserReferal(String userid) {
    this.userid = userid;
    this.timestamp = (new Date()).getTime();
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id The id to set
   */
  public void setId(Long id) {
    this.id = id;
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
