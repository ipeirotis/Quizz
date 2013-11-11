package com.ipeirotis.crowdquiz.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class BadgeAssignment {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key	key;
	
	@Persistent
	private User user;
	
	@Persistent
	private Badge badge;
	
	public BadgeAssignment(User user, Badge badge) {
		this.key = generateKeyFromUserBadge(user, badge);
		this.badge = badge;
		this.user = user;
	}
	
	public static Key generateKeyFromUserBadge(User user, Badge badge) {
		return KeyFactory.createKey(Badge.class.getSimpleName(), "id_" + user.getUserid() + "_" + badge.getBadgename());
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public Badge getBadge() {
		return badge;
	}
	
	public void setBadge(Badge badge) {
		this.badge = badge;
	}
}
