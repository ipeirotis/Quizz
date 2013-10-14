package com.ipeirotis.crowdquiz.servlets;

public class QuestionBean {

	private String	mid;
	private String name;
	
	private Double	weight;

	/**
	 * @return the mid
	 */
	public String getMid() {

		return mid;
	}


	public String getName() {
		return name;
	}

	/**
	 * @return the weight
	 */
	public Double getWeight() {
	
		return weight;
	}

	/**
	 * @param mid
	 *          the mid to set
	 */
	public void setMid(String mid) {

		this.mid = mid;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(Double weight) {
	
		this.weight = weight;
	}

}
