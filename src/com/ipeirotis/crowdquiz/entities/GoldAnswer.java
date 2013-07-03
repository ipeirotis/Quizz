package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GoldAnswer {

	@Persistent
	private String	relation;

	@Persistent
	public String	mid;
	
	@Persistent
	public String	answer;




	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	public GoldAnswer(String relation, String mid, String answer) {

		this.relation = relation;
		this.mid = mid;
		this.answer = answer;


		Key k = generateKeyFromID(relation, mid);
		this.key = k;
	}


	public static Key generateKeyFromID(String relation, String mid) {

		return KeyFactory.createKey(GoldAnswer.class.getSimpleName(), "id_" + relation + "_" + mid);
	}

	
	/**
	 * @return the key
	 */
	public Key getKey() {

		return key;
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
	 * @return the mid
	 */
	public String getMid() {
	
		return mid;
	}


	
	/**
	 * @param mid the mid to set
	 */
	public void setMid(String mid) {
	
		this.mid = mid;
	}


	
	/**
	 * @return the answer
	 */
	public String getAnswer() {
	
		return answer;
	}


	
	/**
	 * @param answer the answer to set
	 */
	public void setAnswer(String answer) {
	
		this.answer = answer;
	}


	/**
	 * @param key
	 *          the key to set
	 */
	public void setKey(Key key) {

		this.key = key;
	}

}

