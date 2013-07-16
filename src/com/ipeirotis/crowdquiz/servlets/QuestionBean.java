package com.ipeirotis.crowdquiz.servlets;

public class QuestionBean {

	private String	mid;
	private Double	weight;

	/**
	 * @return the mid
	 */
	public String getMid() {

		return mid;
	}

	/**
	 * @param mid
	 *          the mid to set
	 */
	public void setMid(String mid) {

		this.mid = mid;
	}

	
	/**
	 * @return the weight
	 */
	public Double getWeight() {
	
		return weight;
	}

	
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(Double weight) {
	
		this.weight = weight;
	}

}
