package com.ipeirotis.crowdquiz.entities;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Objects;

import us.quizz.repository.QuizQuestionRepository;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Question {

	@Persistent
	private String quizID;

	@Persistent
	private Double weight;

	@Persistent
	private String text;

	public String getQuizID() {
		return quizID;
	}

	public void setQuizID(String quizID) {
		this.quizID = quizID;
	}

	public Integer getNumberOfGoldAnswers() {
		return numberOfGoldAnswers;
	}

	public void setNumberOfGoldAnswers(Integer numberOfGoldAnswers) {
		this.numberOfGoldAnswers = numberOfGoldAnswers;
	}

	public Integer getNumberOfSilverAnswers() {
		return numberOfSilverAnswers;
	}

	public void setNumberOfSilverAnswers(Integer numberOfSilverAnswers) {
		this.numberOfSilverAnswers = numberOfSilverAnswers;
	}


	public Boolean getHasGoldAnswer() {
		return hasGoldAnswer;
	}

	public Boolean getHasSilverAnswers() {
		return hasSilverAnswers;
	}

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
    
    @Persistent(defaultFetchGroup = "true")
    private ArrayList<Answer> answers;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	public void setKey(Key key) {
		this.key = key;
	}
	
	public Question(String quizID, String text, Double weight) {
		this.quizID = quizID;
		this.weight = weight;
		this.text = text;
		answers = new ArrayList<Answer>();
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

	public Long getID() {
		return key.getId();
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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
	
	public ArrayList<Answer> getAnswers(){
		return answers;
	}
	
	public void setAnswers(ArrayList<Answer> answers){
		this.answers = answers;
	}
	
	public void addAnswer(Answer answer){
		answers.add(answer);
	}
	
	public Answer getAnswer(Integer answerID){
		return answers.get(answerID);
	}

	@Override
	public boolean equals(Object other){
		if (other instanceof Question) {
			Question o = (Question) other;
			return Objects.equal(getID(), o.getID());
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(getID());
	}
}
