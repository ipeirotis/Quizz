package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserAnswer {

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
	private String	relation;

	@Persistent
	private String	browser;

	@Persistent
	private String	action;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;


	
	public UserAnswer(String userid, String relation, String mid, Long answerID) {

		this.relation = relation.replace('\t', ' ');
		this.answerID = answerID;
		this.userid = userid.replace('\t', ' ');

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
	
	public String getRelation() {
		return relation;
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

	public void setRelation(String relation) {
		this.relation = relation.replace('\t', ' ');
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
