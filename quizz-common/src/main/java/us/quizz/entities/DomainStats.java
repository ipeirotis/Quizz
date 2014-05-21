package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

@Entity
@Cache
@Index
public class DomainStats implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String domain;
  private long userCount = 0;
  private double userScores = 0.0;

  //for Objectify
  @SuppressWarnings("unused")
  private DomainStats(){}

  public DomainStats(String domain, long userCount, double userScores) {
    this.domain = domain;
    this.userCount = userCount;
    this.userScores = userScores;
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

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void incUserCount() {
    this.userCount++;
  }
}
