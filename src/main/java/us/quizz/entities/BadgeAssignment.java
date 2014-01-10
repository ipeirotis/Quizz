package us.quizz.entities;

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
	private String userid;

	@Persistent
	private String badgename;
	
	public BadgeAssignment(String userid, String badgename) {
		this.userid = userid;
		this.badgename = badgename;
		this.key = generateKeyFromUserBadge(userid, badgename);
	}
	
	public static Key generateKeyFromUserBadge(String userid, String badgeid) {
		return KeyFactory.createKey(BadgeAssignment.class.getSimpleName(), "id_" + userid + "_" + badgeid);
	}
	
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getUserid() {
		return userid;
	}
	
	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getBadgename() {
		return badgename;
	}
	
	public void setBadgename(String badgename) {
		this.badgename = badgename;
	}
}
