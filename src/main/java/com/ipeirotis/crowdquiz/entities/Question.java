package com.ipeirotis.crowdquiz.entities;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import us.quizz.repository.QuizQuestionRepository;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Question {

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
	private Boolean hasUserAnswers;
	
	@Persistent
	private Double totalUserScore;
	
    @Persistent
    private Boolean hasGoldAnswer;

    @Persistent
    private Integer numberOfGoldAnswers;

    @Persistent
    private Boolean hasSilverAnswers;

    @Persistent
    private Integer numberOfSilverAnswers;
    
    @Persistent
    private Integer numberOfCorrentUserAnswers;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long key;
	
	@Persistent
	private List<Answer> answers;

	public Question(String quizID, String name, Double weight) {

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

	public Long getKey() {
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
	
	public Long getID() {
		return key;
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
	
	public Integer getNumberOfCorrentUserAnswers() {
		return numberOfCorrentUserAnswers;
	}

	public void setNumberOfCorrentUserAnswers(Integer numberOfCorrentUserAnswers) {
		this.numberOfCorrentUserAnswers = numberOfCorrentUserAnswers;
	}

	public Double getTotalUserScore() {
		return totalUserScore;
	}

	public void setTotalUserScore(Double totalUserScore) {
		this.totalUserScore = totalUserScore;
	}

	public void setHasGoldAnswer(Boolean hasGoldAnswer) {
		this.hasGoldAnswer = hasGoldAnswer;
	}

	public void setHasSilverAnswers(Boolean hasSilverAnswers) {
		this.hasSilverAnswers = hasSilverAnswers;
	}

	public void setHasUserAnswers(Boolean hasUserAnswers) {
		this.hasUserAnswers = hasUserAnswers;
	}
	
	public Boolean getHasUserAnswers(){
		return hasUserAnswers;
	}

}
