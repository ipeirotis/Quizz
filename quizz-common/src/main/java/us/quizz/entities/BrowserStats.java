package us.quizz.entities;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import eu.bitwalker.useragentutils.Browser;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class BrowserStats implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	private Browser browser;
	
	@Persistent
	private String browserName;
	
	@Persistent
	private long userCount = 0;
	
	@Persistent
	private double userScores = 0;

	public BrowserStats(Browser browser, long userCount, double userScores) {
		this.key = KeyFactory.createKey(BrowserStats.class.getSimpleName(), browser.toString());
		this.browser = browser;
		this.browserName = browser.getName();
		this.userCount = userCount;
		this.userScores = userScores;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Browser getBrowser() {
		return browser;
	}

	public void setBrowser(Browser browser) {
		this.browser = browser;
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

	public String getBrowserName() {
		return browserName;
	}

	public void setBrowserName(String browserName) {
		this.browserName = browserName;
	}

}
