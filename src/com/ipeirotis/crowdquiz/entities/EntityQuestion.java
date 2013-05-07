package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class EntityQuestion {

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
	private Double	emptyweight;
	
	@Persistent
	private Long	adGroupId;
	
	@Persistent
	private Long	adTextId;

	
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

	public EntityQuestion(String relation, String freebaseEntityId, Double emptyweight) {

		this.freebaseEntityId = freebaseEntityId;
		this.relation = relation;
		this.emptyweight = emptyweight;

		this.key = generateKeyFromID(relation, freebaseEntityId);
	}

	public static Key generateKeyFromID(String relation, String freebaseEntityId) {

		return KeyFactory.createKey(EntityQuestion.class.getSimpleName(), "id_" + relation + "_" + freebaseEntityId);
	}

	/**
	 * @return the emptyweight
	 */
	public Double getEmptyweight() {

		return emptyweight;
	}

}
