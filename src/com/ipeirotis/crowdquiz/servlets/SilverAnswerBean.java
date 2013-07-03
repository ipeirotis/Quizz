package com.ipeirotis.crowdquiz.servlets;

public class SilverAnswerBean {

	private String	mid;
	private String	answer;
	private Double	probability;
	private String	source;
	
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
	 * @return the probability
	 */
	public Double getProbability() {
	
		return probability;
	}
	
	/**
	 * @param probability the probability to set
	 */
	public void setProbability(Double probability) {
	
		this.probability = probability;
	}
	
	/**
	 * @return the source
	 */
	public String getSource() {
	
		return source;
	}
	
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
	
		this.source = source;
	}

	
}
