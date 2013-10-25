package com.ipeirotis.crowdquiz.servlets;


public class CompletionsEntryBean {

	private String	mid;
	private Double		coverage;
	private Double		filled_entities;
	private Double		unsupported_entities;
	private Double		empty_entities;
	private Double	empty_weight;
	private Double	total_weight;
	private Double	filled_weight;

	public String getMid() {

		return mid;
	}

	public Double getCoverage() {

		return coverage;
	}

	public Double getFilled_entities() {

		return filled_entities;
	}

	public Double getUnsupported_entities() {

		return unsupported_entities;
	}

	public Double getEmpty_entities() {

		return empty_entities;
	}

	public Double getEmpty_weight() {

		return empty_weight;
	}

	public Double getTotal_weight() {

		return total_weight;
	}

	public Double getFilled_weight() {

		return filled_weight;
	}

	public void setMid(String mid) {

		this.mid = mid;
	}

	public void setCoverage(Double coverage) {

		this.coverage = coverage;
	}

	public void setFilled_entities(Double filled_entities) {

		this.filled_entities = filled_entities;
	}

	public void setUnsupported_entities(Double unsupported_entities) {

		this.unsupported_entities = unsupported_entities;
	}

	public void setEmpty_entities(Double empty_entities) {

		this.empty_entities = empty_entities;
	}

	public void setEmpty_weight(Double empty_weight) {

		this.empty_weight = empty_weight;
	}

	public void setTotal_weight(Double total_weight) {

		this.total_weight = total_weight;
	}

	public void setFilled_weight(Double filled_weight) {

		this.filled_weight = filled_weight;
	}

}
