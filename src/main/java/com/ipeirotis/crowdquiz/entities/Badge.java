package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Badge {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key	key;
	
	@Persistent
	private String	badgename;
	
	public Badge(String badgename) {
		this.key = generateKeyFromID(badgename);
		this.badgename = badgename;
	}
	
	public static Key generateKeyFromID(String name) {
		return KeyFactory.createKey(Badge.class.getSimpleName(), "id_" + name);
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getBadgename() {
		return badgename;
	}
	
	public void setBadgename(String badgename) {
		this.badgename = badgename;
	}
}
