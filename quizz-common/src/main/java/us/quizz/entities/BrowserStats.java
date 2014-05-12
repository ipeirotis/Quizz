package us.quizz.entities;

import java.io.Serializable;

import nl.bitwalker.useragentutils.Browser;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
@Index
public class BrowserStats implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String id;
  private Browser browser;
  private String browserName;
  private long userCount = 0;
  private double userScores = 0;

  //for Objectify
  @SuppressWarnings("unused")
  private BrowserStats(){}

  public BrowserStats(Browser browser, long userCount, double userScores) {
    this.id = browser.toString();
    this.browser = browser;
    this.browserName = browser.getName();
    this.userCount = userCount;
    this.userScores = userScores;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Browser getBrowser() {
    return browser;
  }

  public void setBrowser(Browser browser) {
    this.browser = browser;
  }

  public long getUserCount() {
    return userCount;
  }

  public void setUserCount(long userCount) {
    this.userCount = userCount;
  }

  public double getUserScores() {
    return userScores;
  }

  public void setUserScores(double userScores) {
    this.userScores = userScores;
  }

  public String getBrowserName() {
    return browserName;
  }

  public void setBrowserName(String browserName) {
    this.browserName = browserName;
  }
}
