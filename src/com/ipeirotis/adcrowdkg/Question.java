package com.ipeirotis.adcrowdkg;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Question {


	
	/**
	 * @return the freebaseType
	 */
	public String getFreebaseType() {
	
		return freebaseType;
	}


	
	/**
	 * @param freebaseType the freebaseType to set
	 */
	public void setFreebaseType(String freebaseType) {
	
		this.freebaseType = freebaseType;
	}


	/**
	 * @return the questionText
	 */
	public String getQuestionText() {
	
		return questionText;
	}

	
	/**
	 * @param questionText the questionText to set
	 */
	public void setQuestionText(String questionText) {
	
		this.questionText = questionText;
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

	
	/**
	 * @return the freebaseAttribute
	 */
	public String getFreebaseAttribute() {
	
		return freebaseAttribute;
	}



	
	/**
	 * @param freebaseAttribute the freebaseAttribute to set
	 */
	public void setFreebaseAttribute(String freebaseAttribute) {
	
		this.freebaseAttribute = freebaseAttribute;
	}

	
	
	// The name of the relation that we are targeting
	@Persistent
	private String	relation;

	// The question that we will ask to the user
	@Persistent
	private String	questionText;

	// The type of entry for the freebaseAttribute
	@Persistent
	private String	freebaseType;
	
	// The attribute that we are crowdsourcing
	@Persistent
	private String	freebaseAttribute;
	
	@Persistent
	private BlobKey blobKey;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;
	
	public Question(String relation, String questionText, String freebaseAttribute, String freebaseType, BlobKey blobKey) {

		this.questionText = questionText;
		this.relation = relation;
		this.freebaseType = freebaseType;
		this.freebaseAttribute = freebaseAttribute;
		this.blobKey=blobKey;

		Key k = generateKeyFromID(relation);
		this.key = k;
	}
	
	public static Key generateKeyFromID(String id) {
		return KeyFactory.createKey(Question.class.getSimpleName(), "id_" + id);
	}

}
