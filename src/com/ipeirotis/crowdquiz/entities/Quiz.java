package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** 
 * The Quiz is the basic unit of the application. Each quiz contains 
 * a set of QuizQuestions. 
 * 
 * @author ipeirotis
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Quiz {

	public static Key generateKeyFromID(String relation) {

		return KeyFactory.createKey(Quiz.class.getSimpleName(), "id_" + relation);
	}

	// The user-friendly name of the relation that we are targeting
	@Persistent
	private String	category;
	
	// The user-friendly name of the relation that we are targeting
	@Persistent
	private String	name;

	// The name of the relation that we are targeting. 
	// Typically, we assign the name of a KP attribute on this one
	// and serves as a defacto primary key for the quiz.
	@Persistent
	private String	relation;

	// The question that we will ask to the user
	@Persistent
	private String	questionText;

	// The type of entry for the answer that we expect
	// We do not use this for multiple choice questions
	// but it is used for the fill-in questions, to enable
	// autocompletion using the Freebase auto-suggest widget
	@Persistent
	private String	freebaseType;

	// The id of the AdWords ad campaign that brings visitors to the quiz
	@Persistent
	private Long	campaignid;
	
	
	public Long getCampaignid() {
	
		return campaignid;
	}

	
	public void setCampaignid(Long campaignid) {
	
		this.campaignid = campaignid;
	}

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;

	public Quiz(String name, String relation, String questionText) {

		this.name = name;
		this.questionText = questionText;
		this.relation = relation;
		this.freebaseType = null;


		this.key = generateKeyFromID(relation);
	}

	/**
	 * @return the freebaseAttribute
	 */
	/*
	public String getFreebaseAttribute() {

		return freebaseAttribute;
	}
	*/

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

}
