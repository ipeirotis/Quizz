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
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class QuizQuestion {

	public static Key generateKeyFromID(String relation, String freebaseEntityId) {

		return KeyFactory.createKey(QuizQuestion.class.getSimpleName(), "id_"
				+ relation + "_" + freebaseEntityId);
	}

	@Persistent
	private String freebaseEntityId;

	@Persistent
	private String relation;

	@Persistent
	private Double weight;

	@Persistent
	private String name;

	@Persistent
	private Long adGroupId;

	@Persistent
	private Long adTextId;

	@Persistent
	private Boolean hasGoldAnswer;

	@Persistent
	private Integer numberOfGoldAnswers;

	@Persistent
	private Boolean hasSilverAnswers;

	@Persistent
	private Integer numberOfSilverAnswers;
	
	@Persistent
	private Boolean hasUserAnswers;

	@Persistent
	private Integer numberOfUserAnswers;
	
	@Persistent
	private Integer numberOfCorrentUserAnswers;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	public QuizQuestion(String relation, String mid, String name, Double weight) {

		this.freebaseEntityId = mid;
		this.relation = relation;
		this.weight = weight;
		this.name = name;

		this.key = generateKeyFromID(relation, mid);
	}

	public Long getAdGroupId() {

		return adGroupId;
	}

	public Long getAdTextId() {

		return adTextId;
	}

	/**
	 * @return the freebaseEntityId
	 */
	public String getFreebaseEntityId() {

		return freebaseEntityId;
	}



	/**
	 * @return the hasGoldAnswer
	 */
	public Boolean getHasGoldAnswer() {

		return hasGoldAnswer;
	}

	/**
	 * @return the hasSilverAnswers
	 */
	public Boolean getHasSilverAnswers() {

		return hasSilverAnswers;
	}

	/**
	 * @return the key
	 */
	public Key getKey() {

		return key;
	}

	public String getRandomGoldAnswer() {
		return QuizQuestionRepository.getRandomGoldAnswer(this.relation, this.freebaseEntityId);
	}
	
	public Set<String> getIncorrectAnswers(int size) {
		return QuizQuestionRepository.getIncorrectAnswers(this.relation, this.freebaseEntityId, this.name, size);
	}
	
	public List<String> getUserAnswers() {
		return QuizQuestionRepository.getUserAnswers(this.relation, this.freebaseEntityId);
	}

	public ArrayList<String> getGoldAnswers() {
		return QuizQuestionRepository.getGoldAnswers(this.relation, this.freebaseEntityId);
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
	 * @return the numberOfGoldAnswers
	 */
	public Integer getNumberOfGoldAnswers() {

		return numberOfGoldAnswers;
	}

	/**
	 * @return the numberOfSilverAnswers
	 */
	public Integer getNumberOfSilverAnswers() {

		return numberOfSilverAnswers;
	}

	/**
	 * @return the relation
	 */
	public String getRelation() {

		return relation;
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

	/**
	 * @param freebaseEntityId
	 *            the freebaseEntityId to set
	 */
	public void setFreebaseEntityId(String freebaseEntityId) {

		this.freebaseEntityId = freebaseEntityId;
	}

	/**
	 * @param hasGoldAnswer
	 *            the hasGoldAnswer to set
	 */
	public void setHasGoldAnswer(Boolean hasGoldAnswer) {

		this.hasGoldAnswer = hasGoldAnswer;
	}

	/**
	 * @param hasSilverAnswers
	 *            the hasSilverAnswers to set
	 */
	public void setHasSilverAnswers(Boolean hasSilverAnswers) {

		this.hasSilverAnswers = hasSilverAnswers;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(Key key) {

		this.key = key;
	}

	/**
	 * @param numberOfGoldAnswers
	 *            the numberOfGoldAnswers to set
	 */
	public void setNumberOfGoldAnswers(Integer numberOfGoldAnswers) {

		this.numberOfGoldAnswers = numberOfGoldAnswers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param numberOfSilverAnswers
	 *            the numberOfSilverAnswers to set
	 */
	public void setNumberOfSilverAnswers(Integer numberOfSilverAnswers) {

		this.numberOfSilverAnswers = numberOfSilverAnswers;
	}

	/**
	 * @param relation
	 *            the relation to set
	 */
	public void setRelation(String relation) {

		this.relation = relation;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(Double weight) {

		this.weight = weight;
	}

	public Boolean getHasUserAnswers() {
		return hasUserAnswers;
	}

	public void setHasUserAnswers(Boolean hasUserAnswers) {
		this.hasUserAnswers = hasUserAnswers;
	}

	public Integer getNumberOfUserAnswers() {
		return numberOfUserAnswers;
	}

	public void setNumberOfUserAnswers(Integer numberOfUserAnswers) {
		this.numberOfUserAnswers = numberOfUserAnswers;
	}

	public Integer getNumberOfCorrentUserAnswers() {
		return numberOfCorrentUserAnswers;
	}

	public void setNumberOfCorrentUserAnswers(Integer numberOfCorrentUserAnswers) {
		this.numberOfCorrentUserAnswers = numberOfCorrentUserAnswers;
	}

}
