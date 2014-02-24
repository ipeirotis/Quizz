package us.quizz.entities;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class DomainStats implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private String domain;
	
	@Persistent
	private long userCount = 0;
	
	@Persistent
	private double userScores = 0;

	public DomainStats(String domain, long userCount, double userScores) {
		this.key = KeyFactory.createKey(DomainStats.class.getSimpleName(), domain);
		this.domain = domain;
		this.userCount = userCount;
		this.userScores = userScores;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public long getUserCount() {
		return userCount;
	}

	public void setUserCount(long userCount) {
		this.userCount = userCount;
	}

	public double getUserScores() {
		return userScores;
	}

	public void setUserScores(double userScores) {
		this.userScores = userScores;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public void incUserCount() {
		this.userCount++;
	}
	

}
