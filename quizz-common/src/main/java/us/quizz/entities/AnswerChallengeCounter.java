package us.quizz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class AnswerChallengeCounter {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String quizID;
	
	@Persistent
	private Long questionID;
	
	@Persistent
	private Long count = 0L;

	public AnswerChallengeCounter(String quizID, Long questionID) {
		this.key = generateKey(quizID, questionID);
		this.quizID = quizID;
		this.questionID = questionID;
	}
	
	public static Key generateKey(String quizID, Long questionID) {
		return KeyFactory.createKey(AnswerChallengeCounter.class.getSimpleName(), 
				quizID + "_" + questionID);
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
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
}
