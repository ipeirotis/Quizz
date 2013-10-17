package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserAnswer {
	
    public static Key generateKeyFromID(String questionID, String userID) {
        return KeyFactory.createKey(UserAnswer.class.getSimpleName(), "id_" + questionID + "_" + userID);
}


	@Persistent
	private String	userid;
	
	@Persistent
	private Long	timestamp;
	
	@Persistent
	private String	ipaddress;

	@Persistent
	private Long answerID;
	
	@Persistent
	private Double score;

	public Double getScore() {
		return score;
	}

	@Persistent
	private String	referer;

	@Persistent
	private Long questionID;

	@Persistent
	private String	browser;

	@Persistent
	private String	action;
	
    @Persistent
    private Boolean isCorrect;

    public Boolean getIsCorrect() {
            return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
    }

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;


	
	public UserAnswer(String userid, Long questionID, Long answerID) {
		this.questionID = questionID;
		this.answerID = answerID;
		this.userid = userid;
	}
	
	public UserAnswer(String userid, String questionID, String answerID) {
		this(userid, Long.parseLong(questionID), Long.parseLong(answerID));
	}

	public String getAction() {
		return action;
	}
	
	public String getBrowser() {
		return browser;
	}

	/**
	 * @return the freebaseanswer
	 */
	public String getReferer() {
		return referer;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public Key getKey() {
		return key;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Long getAnswerID() {
		return answerID;
	}

	public String getUserid() {
		return userid;
	}
	
	public Long getQuestionID(){
		return questionID;
	}

	public void setAction(String action) {
		this.action = action.replace('\t', ' ');
	}

	public void setBrowser(String browser) {
		this.browser = browser.replace('\t', ' ');
	}

	/**
	 * @param freebaseanswer
	 *          the freebaseanswer to set
	 */
	public void setReferer(String referer) {
		this.referer = referer.replace('\t', ' ');
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress.replace('\t', ' ');
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @param useranswer
	 *          the useranswer to set
	 */
	public void setAnswerID(Long answerID) {
		this.answerID = answerID;
	}

	public void setUserid(String userid) {
		this.userid = userid.replace('\t', ' ');
	}

}
