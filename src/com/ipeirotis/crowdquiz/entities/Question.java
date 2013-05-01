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

	public static Key generateKeyFromID(String relation) {

		return KeyFactory.createKey(Question.class.getSimpleName(), "id_" + relation);
	}

	// The user-friendly name of the relation that we are targeting
	@Persistent
	private String	name;

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
	
	// The element from the attribute that we are crowdsourcing (when the attribute is a compound)
	@Persistent
	private String	freebaseElement;
	
	@Persistent
	private BlobKey	blobKey;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	public Question(String name, String relation, String questionText, String freebaseAttribute, String freebaseElement, String freebaseType, BlobKey blobKey) {

		this.name = name;
		this.questionText = questionText;
		this.relation = relation;
		this.freebaseType = freebaseType;
		this.freebaseAttribute = freebaseAttribute;
		this.freebaseElement = freebaseElement;
		this.blobKey = blobKey;

		this.key = generateKeyFromID(relation);
	}

	/**
	 * @return the freebaseAttribute
	 */
	public String getFreebaseAttribute() {

		return freebaseAttribute;
	}

	/**
	 * @return the freebaseType
	 */
	public String getFreebaseType() {

		return freebaseType;
	}

	/**
	 * @return the key
	 */
	public Key getKey() {

		return key;
	}

	public String getName() {
	
		return name;
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

	
	public String getFreebaseElement() {
	
		return freebaseElement;
	}

}
