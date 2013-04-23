package com.ipeirotis.crowdquiz.entities;

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
	 * @return the questionText
	 */
	public String getQuestionText() {
	
		return questionText;
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
	 * @return the freebaseAttribute
	 */
	public String getFreebaseAttribute() {
	
		return freebaseAttribute;
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

		this.key = generateKeyFromID(relation);
	}
	
	public static Key generateKeyFromID(String id) {
		return KeyFactory.createKey(Question.class.getSimpleName(), "id_" + id);
	}

}
