package us.quizz.entities;

import java.io.Serializable;

import us.quizz.enums.UserActionKind;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
@Index
public class UserAction implements Serializable {
  private static final long serialVersionUID = 1L;
  
  @Id
  private Long id;
  
  private String userid;
  private Long timestamp;
  private UserActionKind kind;
  
  //for Objectify
  @SuppressWarnings("unused")
  private UserAction(){}

  public UserAction(String userid, Long timestamp,  UserActionKind kind) {
    this.userid = userid;
    this.timestamp = timestamp;
    this.kind = kind;
  }

  public String getUserid() {
    return userid;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public UserActionKind getKind() {
    return kind;
  }

  public void setKind(UserActionKind kind) {
    this.kind = kind;
  }
  

}
