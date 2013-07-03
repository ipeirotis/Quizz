package com.ipeirotis.crowdquiz.servlets;



public class UserAnswerBean {

	private String	relation;
	private String	userid;
	private String	mid;
	private String	action;
	private String	answer;
	
	private Long	timestamp;
	private String	ipaddress;
	private String	browser;
	private String	referer;
	
	
	/**
	 * @return the referer
	 */
	public String getReferer() {
	
		return referer;
	}

	
	/**
	 * @param referer the referer to set
	 */
	public void setReferer(String referer) {
	
		this.referer = referer;
	}

	/**
	 * @return the relation
	 */
	public String getRelation() {
	
		return relation;
	}
	
	/**
	 * @param relation the relation to set
	 */
	public void setRelation(String relation) {
	
		this.relation = relation;
	}
	
	/**
	 * @return the userid
	 */
	public String getUserid() {
	
		return userid;
	}
	
	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
	
		this.userid = userid;
	}
	
	/**
	 * @return the mid
	 */
	public String getMid() {
	
		return mid;
	}
	
	/**
	 * @param mid the mid to set
	 */
	public void setMid(String mid) {
	
		this.mid = mid;
	}
	
	/**
	 * @return the action
	 */
	public String getAction() {
	
		return action;
	}
	
	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
	
		this.action = action;
	}
	
	/**
	 * @return the answer
	 */
	public String getAnswer() {
	
		return answer;
	}
	
	/**
	 * @param answer the answer to set
	 */
	public void setAnswer(String answer) {
	
		this.answer = answer;
	}
	
	/**
	 * @return the timestamp
	 */
	public Long getTimestamp() {
	
		return timestamp;
	}
	
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Long timestamp) {
	
		this.timestamp = timestamp;
	}
	
	/**
	 * @return the ipaddress
	 */
	public String getIpaddress() {
	
		return ipaddress;
	}
	
	/**
	 * @param ipaddress the ipaddress to set
	 */
	public void setIpaddress(String ipaddress) {
	
		this.ipaddress = ipaddress;
	}
	
	/**
	 * @return the browser
	 */
	public String getBrowser() {
	
		return browser;
	}
	
	/**
	 * @param browser the browser to set
	 */
	public void setBrowser(String browser) {
	
		this.browser = browser;
	}
	
}
