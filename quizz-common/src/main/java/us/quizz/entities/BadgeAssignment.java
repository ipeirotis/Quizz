package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

@Entity
@Cache
@Index
public class BadgeAssignment implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String id;
  private String userid;
  private String badgename;

  //for Objectify
  @SuppressWarnings("unused")
  private BadgeAssignment(){}

  public BadgeAssignment(String userid, String badgename) {
    this.userid = userid;
    this.badgename = badgename;
    this.id = generateId(userid, badgename);
  }

  public static String generateId(String userid, String badgeid) {
    return "id_" + userid + "_" + badgeid;
  }

  public String getUserid() {
    return userid;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  public String getBadgename() {
    return badgename;
  }

  public void setBadgename(String badgename) {
    this.badgename = badgename;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
