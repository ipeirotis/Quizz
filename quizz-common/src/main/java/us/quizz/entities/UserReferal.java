package us.quizz.entities;

import com.google.appengine.api.datastore.Text;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import nl.bitwalker.useragentutils.Browser;

import java.io.Serializable;
import java.util.Date;

@Entity
@Cache
@Index
public class UserReferal implements Serializable {
  public static final String QUIZ_LANDING_PAGE = "HOMEPAGE";
  private static final long serialVersionUID = 1L;

  @Id
  private Long id;
  private String userid;
  // Quiz id of the page when the user first visits on quizz.us. If user comes directly to the
  // homepage of Quizz (i.e. organic visit, not through referal from Ads etc), the value will be
  // QUIZ_LANDING_PAGE.
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getQuiz() {
    return quiz;
  }

  public void setQuiz(String quiz) {
    this.quiz = quiz;
  }

  public String getIpaddress() {
    return ipaddress;
  }

  public void setIpaddress(String ipaddress) {
    // anonymizing the last 4 digits
    ipaddress = ipaddress.substring(0, ipaddress.length() - 4);
    this.ipaddress = ipaddress + "XXXX";
  }

  public Text getReferer() {
    return referer;
  }

  public void setReferer(String referer) {
    if (referer != null) {
      this.referer = new Text(referer);
    } else {
      this.referer = null;
    }
  }

  public String getUserid() {
    return userid;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public Browser getBrowser() {
    return browser;
  }

  public void setBrowser(Browser browser) {
    this.browser = browser;
  }
}
