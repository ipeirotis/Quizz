package com.ipeirotis.crowdquiz.entities;

import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;


	// The id for the user.
	@Persistent
	private String	userid;
	
	// The id for the user's session.
	@Persistent
	private String	sessionid;
	
	//The id for the user's fb or google account.
	@Persistent
	private String socialid;
	
	// The set of treatments assigned to the user
	@Persistent(defaultFetchGroup = "true")
	private Experiment experiment;
	
	
	public User(String userid) {
		this.userid = userid;
		this.key = generateKeyFromID(userid);
	}
	
	public static Key generateKeyFromID(String userid) {
		return KeyFactory.createKey(User.class.getSimpleName(), "id_" + userid);
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public String getSocialid() {
		return socialid;
	}
	
	public void setSocialid(String sid) {
		this.socialid = sid;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public boolean getsTreatment(String treatmentName) {
		return this.experiment.getsTreatment(treatmentName);
	}
	
	public Map<String, Boolean> getTreatments() {
		return this.experiment.treatments;
	}
	
}
