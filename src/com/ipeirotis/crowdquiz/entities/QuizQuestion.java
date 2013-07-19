package com.ipeirotis.crowdquiz.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ipeirotis.crowdquiz.utils.PMF;

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

	public static ArrayList<String> getGoldAnswers(String relation, String mid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", relation);
		params.put("midParam", mid);

		List<GoldAnswer> qresults = (List<GoldAnswer>) q.executeWithMap(params);
		pm.close();

		ArrayList<String> result = new ArrayList<String>();
		for (GoldAnswer ga : qresults) {
			result.add(ga.getAnswer());
		}

		return result;
	}

	private static ArrayList<String> getGoldAnswersNoCache(String relation) {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(GoldAnswer.class);
		q.setFilter("relation == quizParam");
		q.declareParameters("String quizParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", relation);

		List<GoldAnswer> qresults = (List<GoldAnswer>) q.executeWithMap(params);
		pm.close();

		ArrayList<String> result = new ArrayList<String>();
		for (GoldAnswer ga : qresults) {
			result.add(ga.getAnswer());
		}

		return result;
	}

	public static ArrayList<String> getGoldAnswers(String relation) {

		Cache cache;

		try {
			CacheFactory cacheFactory = CacheManager.getInstance()
					.getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException e) {
			cache = null;
		}

		String key = "getgoldanswers_" + relation;

		ArrayList<String> result = new ArrayList<String>();
		if (cache != null && cache.containsKey(key)) {
			byte[] value = (byte[]) cache.get(key);
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new ByteArrayInputStream(value));
				result = (ArrayList<String>) in.readObject();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			result = getGoldAnswersNoCache(relation);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			cache.put(key, bos.toByteArray());
		}

		return result;
	}

	public ArrayList<String> getGoldAnswers() {
		return QuizQuestion.getGoldAnswers(this.relation, this.freebaseEntityId);
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
		// First we need to put one correct result
		ArrayList<String> gold = getGoldAnswers();
		if (gold.size() == 0) {
			return null;
		}

		// Select one gold answer at random and put it in the results
		int r = (int) Math.round(Math.random() * gold.size());
		if (r >= gold.size()) {
			r = gold.size() - 1;
		}
		return gold.get(r);
	}
	
	public Set<String> getIncorrectAnswers(int size) {
		Set<String> results = new TreeSet<String>();

		// Get a set of potential answers from other questions
		List<String> wrongAnswers = getGoldAnswers(this.relation);
		
		// Remove any self-reference
		wrongAnswers.remove(this.name);
		
		// Remove all gold answers
		ArrayList<String> gold = getGoldAnswers();
		wrongAnswers.removeAll(gold);

		// Get a list of potential good answers from KV and remove them
		// from the list of results. We want only clearly incorrect answers
		List<String> good_silver = getSilverAnswers(true, 0.5);
		wrongAnswers.removeAll(good_silver);

		while (results.size() < size && wrongAnswers.size()>0) {
			int rnd = (int) Math.round(Math.random() * wrongAnswers.size());
			if (rnd >= wrongAnswers.size()) {
				rnd = wrongAnswers.size() - 1;
			}
			String candidate = wrongAnswers.get(rnd);
			wrongAnswers.remove(rnd);
			results.add(candidate);
		}

		return results;
	}
	
	
	public Set<String> getMultipleChoice(int size) {
		Set<String> results = new TreeSet<String>();

		String gold = getRandomGoldAnswer();
		results.add(gold);
		
		Set<String> pyrite = getIncorrectAnswers(size-1);
		results.addAll(pyrite);
		
		return results;
	}

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

	public List<String> getSilverAnswers(boolean highprobability,
			double prob_threshold) {

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(SilverAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", this.relation);
		params.put("midParam", this.freebaseEntityId);
		List<SilverAnswer> answers = (List<SilverAnswer>) q
				.executeWithMap(params);
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
			// if (ue.getUserid().equals(ignoreUser)) {
			// continue;
			// }

		}
		return result;

	}

	public List<String> getUserAnswers() {

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(UserAnswer.class);
		q.setFilter("relation == quizParam && mid == midParam");
		q.declareParameters("String quizParam, String midParam");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("quizParam", this.relation);
		params.put("midParam", this.freebaseEntityId);
		List<UserAnswer> answers = (List<UserAnswer>) q.executeWithMap(params);
		pm.close();

		List<String> result = new ArrayList<String>();
		for (UserAnswer ue : answers) {
			// if (ue.getUserid().equals(ignoreUser)) {
			// continue;
			// }
			result.add(ue.getUseranswer());

		}
		return result;

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

}
