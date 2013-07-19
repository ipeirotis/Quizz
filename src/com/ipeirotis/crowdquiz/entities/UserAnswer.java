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

	public static Key generateKeyFromID(String userid, String relation, String mid) {

		return KeyFactory.createKey(UserAnswer.class.getSimpleName(), "id_" + userid + "_" + relation + "_" + mid);
	}

	@Persistent
	private String	userid;
	
	@Persistent
	private Long	timestamp;
	
	
	@Persistent
	private String	ipaddress;

	
	@Persistent
	private String	useranswer;
	
	@Persistent
	private Boolean isCorrect;

	public Boolean getIsCorrect() {
		return isCorrect;
	}

	public void setIsCorrect(Boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	@Persistent
	private String	referer;

	@Persistent
	private String	relation;

	@Persistent
	private String	browser;

	@Persistent
	private String	action;


	@Persistent
	private String	mid;

	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;


	
	public UserAnswer(String userid, String relation, String mid, String useranswer) {

		this.relation = relation.replace('\t', ' ');
		this.mid = mid.replace('\t', ' ');
		this.useranswer = useranswer.replace('\t', ' ');
		this.userid = userid.replace('\t', ' ');


		Key k = generateKeyFromID(userid, relation, mid);
		this.key = k;
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

	/**
	 * @return the key
	 */
	public Key getKey() {

		return key;
	}

	/**
	 * @return the mid
	 */
	public String getMid() {

		return mid;
	}

	
	/**
	 * @return the relation
	 */
	public String getRelation() {

		return relation;
	}


	
	public Long getTimestamp() {
	
		return timestamp;
	}


	
	/**
	 * @return the useranswer
	 */
	public String getUseranswer() {

		return useranswer;
	}


	/**
	 * @return the userid
	 */
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

	/**
	 * @param key
	 *          the key to set
	 */
	public void setKey(Key key) {

		this.key = key;
	}

	/**
	 * @param mid
	 *          the mid to set
	 */
	public void setMid(String mid) {

		this.mid = mid.replace('\t', ' ');
	}

	/**
	 * @param relation
	 *          the relation to set
	 */
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
	public void setUseranswer(String useranswer) {

		this.useranswer = useranswer.replace('\t', ' ');
	}

	public void setUserid(String userid) {
	
		this.userid = userid.replace('\t', ' ');
	}

}
