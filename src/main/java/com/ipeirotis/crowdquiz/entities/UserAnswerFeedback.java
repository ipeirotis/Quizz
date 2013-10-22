package com.ipeirotis.crowdquiz.entities;

import java.text.NumberFormat;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** 
 * The Quiz is the basic unit of the application. Each quiz contains 
 * a set of QuizQuestions. 
 * 
 * @author ipeirotis
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserAnswerFeedback {

	public static Key generateKeyFromID(Long questionID, String userid) {
		return KeyFactory.createKey(UserAnswerFeedback.class.getSimpleName(), "id_" + questionID + "_" + userid);
	}
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private Long questionID;
	
	@Persistent
	private String	userid;

	@Persistent
	private Long	userAnswerID;
	
	@Persistent
	private Boolean isCorrect;

	@Persistent
	private String	correctAnswer;
	
	@Persistent
	private Integer numCorrectAnswers;
	
	@Persistent
	private Integer numTotalAnswers;
	
	@Persistent
	private String	difficulty;

	public UserAnswerFeedback(Long questionID, String userid, Long userAnswerID, Boolean isCorrect) {

		this.questionID = questionID;
		this.userid = userid;
		this.userAnswerID = userAnswerID;
		this.isCorrect = isCorrect;

		this.key = generateKeyFromID(questionID, userid);
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Long getQuestionID() {
		return questionID;
	}

	public void setQuestionID(Long questionID) {
		this.questionID = questionID;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public Long getUserAnswerID() {
		return userAnswerID;
	}

	public void setUserAnswerID(Long userAnswerID) {
		this.userAnswerID = userAnswerID;
	}

	public Boolean getIsCorrect() {
		return isCorrect;
	}

	public void setIsCorrect(Boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	public String getCorrectAnswer() {
		return correctAnswer;
	}

	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}

	public Integer getNumCorrectAnswers() {
		return numCorrectAnswers;
	}

	public void setNumCorrectAnswers(Integer numCorrectAnswers) {
		this.numCorrectAnswers = numCorrectAnswers;
	}

	public Integer getNumTotalAnswers() {
		return numTotalAnswers;
	}

	public void setNumTotalAnswers(Integer numTotalAnswers) {
		this.numTotalAnswers = numTotalAnswers;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void computeDifficulty() {
		if (numTotalAnswers==null || numCorrectAnswers==null || numTotalAnswers==0) {
			this.difficulty = "--";
			return;
		}
		
		double d = 1.0*this.numCorrectAnswers/this.numTotalAnswers;
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(0);
		this.difficulty = percentFormat.format(d);
		
	}


}
