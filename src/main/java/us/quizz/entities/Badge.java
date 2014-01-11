package us.quizz.entities;

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
	private Key key;

	@Persistent
	private String badgename;

	@Persistent
	private String shortname;

	public Badge(String badgename, String shortname) {
		this.key = generateKeyFromID(badgename);
		this.badgename = badgename;
		this.shortname = shortname;
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

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
}
