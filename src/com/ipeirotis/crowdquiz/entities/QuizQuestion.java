package com.ipeirotis.crowdquiz.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ipeirotis.crowdquiz.utils.PMF;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class QuizQuestion {

	/**
	 * @return the freebaseEntityId
	 */
	public String getFreebaseEntityId() {

		return freebaseEntityId;
	}

	/**
	 * @return the relation
	 */
	public String getRelation() {

		return relation;
	}

	/**
	 * @return the key
	 */
	public Key getKey() {

		return key;
	}

	/**
	 * @param key
	 *          the key to set
	 */
	public void setKey(Key key) {

		this.key = key;
	}

	@Persistent
	private String	freebaseEntityId;

	@Persistent
	private String	relation;

	@Persistent
	private Double	weight;
	
	@Persistent
	private Long	adGroupId;
	
	@Persistent
	private Long	adTextId;

	
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

	
	/**
	 * @return the hasGoldAnswer
	 */
	public Boolean getHasGoldAnswer() {
	
		return hasGoldAnswer;
	}

	
	/**
	 * @param hasGoldAnswer the hasGoldAnswer to set
	 */
	public void setHasGoldAnswer(Boolean hasGoldAnswer) {
	
		this.hasGoldAnswer = hasGoldAnswer;
	}

	
	/**
	 * @param freebaseEntityId the freebaseEntityId to set
	 */
	public void setFreebaseEntityId(String freebaseEntityId) {
	
		this.freebaseEntityId = freebaseEntityId;
	}

	
	/**
	 * @param relation the relation to set
	 */
	public void setRelation(String relation) {
	
		this.relation = relation;
	}

	@Persistent
	private Boolean	hasGoldAnswer;
	
	@Persistent
	private Integer	numberOfGoldAnswers;

	@Persistent
	private Boolean	hasSilverAnswers;
	
	
	/**
	 * @return the numberOfGoldAnswers
	 */
	public Integer getNumberOfGoldAnswers() {
	
		return numberOfGoldAnswers;
	}

	
	/**
	 * @param numberOfGoldAnswers the numberOfGoldAnswers to set
	 */
	public void setNumberOfGoldAnswers(Integer numberOfGoldAnswers) {
	
		this.numberOfGoldAnswers = numberOfGoldAnswers;
	}

	
	/**
	 * @return the hasSilverAnswers
	 */
	public Boolean getHasSilverAnswers() {
	
		return hasSilverAnswers;
	}

	
	/**
	 * @param hasSilverAnswers the hasSilverAnswers to set
	 */
	public void setHasSilverAnswers(Boolean hasSilverAnswers) {
	
		this.hasSilverAnswers = hasSilverAnswers;
	}

	
	/**
	 * @return the numberOfSilverAnswers
	 */
	public Integer getNumberOfSilverAnswers() {
	
		return numberOfSilverAnswers;
	}

	
	/**
	 * @param numberOfSilverAnswers the numberOfSilverAnswers to set
	 */
	public void setNumberOfSilverAnswers(Integer numberOfSilverAnswers) {
	
		this.numberOfSilverAnswers = numberOfSilverAnswers;
	}

	@Persistent
	private Integer	numberOfSilverAnswers;
	
	public Long getAdGroupId() {
	
		return adGroupId;
	}

	
	public void setAdGroupId(Long adGroupId) {
	
		this.adGroupId = adGroupId;
	}

	
	public Long getAdTextId() {
	
		return adTextId;
	}

	
	public void setAdTextId(Long adTextId) {
	
		this.adTextId = adTextId;
	}

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	public QuizQuestion(String relation, String freebaseEntityId, Double weight) {

		this.freebaseEntityId = freebaseEntityId;
		this.relation = relation;
		this.weight = weight;

		this.key = generateKeyFromID(relation, freebaseEntityId);
	}

	public static Key generateKeyFromID(String relation, String freebaseEntityId) {

		return KeyFactory.createKey(QuizQuestion.class.getSimpleName(), "id_" + relation + "_" + freebaseEntityId);
	}

	public Set<String> getMultipleChoice(int size) {
		Set<String> results = new TreeSet<String>();
		
		// First we need to put one correct result
		List<String> g = getGoldAnswers();
		
		if (g.size()==0) {
			// TODO: Handle the case
			// Check for high confidence silvers
			// If hc silver not available, check for crowd answers 
		}
		int r = (int)Math.round(Math.random()*g.size());
		if (r>=g.size()) r = g.size()-1;
		results.add(g.get(r));
		
		List<String> s = getSilverAnswers(false, 0.5);
		for (String a: s) {
			if (results.size()<size) results.add(a);
		}
		
		//List<String> u = getUserAnswers();
		
		return results;
	}
	
	
	public List<String> getGoldAnswers() {
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		
		Query q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String,Object> params = new HashMap<String, Object>();
		params.put("quizParam", this.relation);
    params.put("midParam", this.freebaseEntityId);
    
		List<GoldAnswer> qresults = (List<GoldAnswer>) q.executeWithMap(params);
		pm.close();
		
		List<String> result = new ArrayList<String>();
		for (GoldAnswer ga: qresults) {
			result.add(ga.getAnswer());
		}
		
		return result;
	}
	
	
	public List<String> getUserAnswers() {
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String,Object> params = new HashMap<String, Object>();
		params.put("quizParam", this.relation);
    params.put("midParam", this.freebaseEntityId);
		List<UserAnswer> answers = (List<UserAnswer>) q.executeWithMap(params);
		pm.close();

		List<String> result = new ArrayList<String>();
		for (UserAnswer ue : answers) {
			//if (ue.getUserid().equals(ignoreUser)) {
			//	continue;
			//}
			result.add(ue.getUseranswer());
			
		}
		return result;

	}
	
	public List<String> getSilverAnswers(boolean highprobability, double prob_threshold) {
		
		PersistenceManager	pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(SilverAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String,Object> params = new HashMap<String, Object>();
		params.put("quizParam", this.relation);
    params.put("midParam", this.freebaseEntityId);
		List<SilverAnswer> answers = (List<SilverAnswer>) q.executeWithMap(params);
		pm.close();

		List<String> result = new ArrayList<String>();
		for (SilverAnswer ue : answers) {
			if (highprobability) {
				if (ue.getProbability() >= prob_threshold) {
					result.add(ue.getAnswer());
				} 
			} else {
				if (ue.getProbability() <= prob_threshold) {
					result.add(ue.getAnswer());
				} 
			}
			//if (ue.getUserid().equals(ignoreUser)) {
			//	continue;
			//}
			
		}
		return result;

	}
	

}
