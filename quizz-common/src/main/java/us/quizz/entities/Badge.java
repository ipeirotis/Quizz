package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

@Entity
@Cache
@Index
public class Badge implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String badgename;
  private String shortname;

  //for Objectify
  @SuppressWarnings("unused")
  private Badge(){}

  public Badge(String badgename, String shortname) {
    this.badgename = badgename;
    this.shortname = shortname;
  }

  public String getBadgename() {
    return badgename;
  }

  public void setBadgename(String badgename) {
    this.badgename = badgename;
  }

  public String getShortname() {
    return shortname;
  }

  public void setShortname(String shortname) {
    this.shortname = shortname;
  }
}
