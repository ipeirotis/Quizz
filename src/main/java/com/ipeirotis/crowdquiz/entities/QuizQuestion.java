package com.ipeirotis.crowdquiz.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import us.quizz.repository.QuizQuestionRepository;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class QuizQuestion {

	@Persistent
	private String quizID;

	@Persistent
	private Double weight;

	@Persistent
	private String name;

	@Persistent
	private Long adGroupId;

	@Persistent
	private Long adTextId;

	@Persistent
	private Integer numberOfUserAnswers;
	
	@Persistent
	private Double totalUserScore;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private List<QuizAnswer> answers;

	public QuizQuestion(String quizID, String name, Double weight) {

		this.quizID = quizID;
		this.weight = weight;
		this.name = name;
	}

	public Long getAdGroupId() {
		return adGroupId;
	}

	public Long getAdTextId() {
		return adTextId;
	}

	public Key getKey() {
		return key;
	}
	
	public List<UserAnswer> getUserAnswers() {
		return QuizQuestionRepository.getUserAnswers(this);
	}

	
	/*
	public Set<String> getMultipleChoice(int size) {
		
		String cachekey = "quizquestion-choices-"+this.relation+this.freebaseEntityId;
		Set<String> results = CachePMF.get(cachekey, Set.class);
		if (results != null) return results;
		
		results = new TreeSet<String>();
		
		String gold = getRandomGoldAnswer();
		results.add(gold);
		
		Set<String> pyrite = getIncorrectAnswers(size-1);
		results.addAll(pyrite);
		
		CachePMF.put("quizquestion-choices-"+this.relation+this.freebaseEntityId, results);
		
		return results;
	}
	*/

	/**
	 * @return the relation
	 */
	public String getQuizzID() {
		return quizID;
	}

	/**
	 * @return the weight
	 */
	public Double getWeight() {
		return weight;
	}

	public void setAdGroupId(Long adGroupId) {
		this.adGroupId = adGroupId;
	}

	public void setAdTextId(Long adTextId) {
		this.adTextId = adTextId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRelation(String quizID) {
		this.quizID = quizID;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public Boolean hasUserAnswers() {
		return numberOfUserAnswers > 0;
	}

	public Integer getNumberOfUserAnswers() {
		return numberOfUserAnswers;
	}

	public void setNumberOfUserAnswers(Integer numberOfUserAnswers) {
		this.numberOfUserAnswers = numberOfUserAnswers;
	}

	public Double getTotalUserScore() {
		return totalUserScore;
	}

	public void setTotalUserScore(Double totalUserScore) {
		this.totalUserScore = totalUserScore;
	}

}
