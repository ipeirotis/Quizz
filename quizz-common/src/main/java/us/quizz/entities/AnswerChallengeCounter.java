package us.quizz.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
@Index
public class AnswerChallengeCounter {
  @Id
  private String id;

  private String quizID;

  private Long questionID;

  private Long count = 0L;
  
  //for Objectify
  @SuppressWarnings("unused")
  private AnswerChallengeCounter(){}

  public AnswerChallengeCounter(String quizID, Long questionID) {
    this.id = generateId(quizID, questionID);
    this.quizID = quizID;
    this.questionID = questionID;
  }

  public static String generateId(String quizID, Long questionID) {
    return quizID + "_" + questionID;
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

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public void incCount() {
    this.count++;
  }

  public void decCount() {
    this.count--;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
