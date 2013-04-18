package com.ipeirotis.adcrowdkg;

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
	 * @param freebaseEntityId the freebaseEntityId to set
	 */
	public void setFreebaseEntityId(String freebaseEntityId) {
	
		this.freebaseEntityId = freebaseEntityId;
	}

	
	/**
	 * @return the relation
	 */
	public String getRelation() {
	
		return relation;
	}

	
	/**
	 * @param relation the relation to set
	 */
	public void setRelation(String relation) {
	
		this.relation = relation;
	}

	
	/**
	 * @return the key
	 */
	public Key getKey() {
	
		return key;
	}

	
	/**
	 * @param key the key to set
	 */
	public void setKey(Key key) {
	
		this.key = key;
	}

	@Persistent
	private String	freebaseEntityId;

	@Persistent
	private String	relation;

	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;
	
	public EntityQuestion(String relation, String freebaseEntityId) {

		this.freebaseEntityId = freebaseEntityId;
		this.relation = relation;

		Key k = KeyFactory.createKey(EntityQuestion.class.getSimpleName(), "id_" + relation + "_" + freebaseEntityId);
		this.key = k;
	}

}
