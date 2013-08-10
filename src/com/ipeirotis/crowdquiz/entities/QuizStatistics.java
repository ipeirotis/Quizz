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
public class QuizStatistics {

	public static Key generateKeyFromID(String relation) {

		return KeyFactory.createKey(QuizStatistics.class.getSimpleName(), "id_" + relation);
	}

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	// The id of the Quiz
	@Persistent
	private String	relation;


	

	

	
}
