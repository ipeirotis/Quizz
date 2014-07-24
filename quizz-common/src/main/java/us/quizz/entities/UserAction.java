package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import us.quizz.enums.UserActionKind;

import java.io.Serializable;

@Entity
@Cache
@Index
public class UserAction implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private Long id;

  // Required fields.
  private String userid;
  private Long timestamp;
  private UserActionKind kind;

  // Optional fields only used for certain UserAction.
  // Required for:
  // QUESTION_SHOWN, ANSWER_SENT, ANSWER_SKIPPED, EXPAND_QUESTION_CONTEXT, HIDE_QUESTION_CONTEXT.
  private String quizID;

  // Required for:
  // QUESTION_SHOWN, ANSWER_SENT, ANSWER_SKIPPED, EXPAND_QUESTION_CONTEXT, HIDE_QUESTION_CONTEXT.
  private Long questionID;

  // Optional for:
  // ANSWER_SENT (if it is a multiple choice question).
  private Integer answerID;

  // Optional for:
  // ANSWER_SENT (if it is a free text question).
  private String userAnswer;

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

  public String getQuizID() {
    return quizID;
  }

  public void setQuizID(String quizID) {
    this.quizID = quizID;
  }

  public Long getQuestionID() {
    return questionID;
  }

  public void setQuestionID(Long questionID) {
    this.questionID = questionID;
  }

  public Integer getAnswerID() {
    return answerID;
  }

  public void setAnswerID(Integer answerID) {
    this.answerID = answerID;
  }

  public String getUserAnswer() {
    return userAnswer;
  }

  public void setUserAnswer(String userAnswer) {
    this.userAnswer = userAnswer;
  }
}
