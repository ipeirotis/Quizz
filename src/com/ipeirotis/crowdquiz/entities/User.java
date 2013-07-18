package com.ipeirotis.crowdquiz.entities;

import java.util.Map;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.ipeirotis.crowdquiz.utils.PMF;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key			key;


	// The id for the user.
	@Persistent
	private String	userid;
	
	// The set of treatments assigned to the user
	@Persistent(defaultFetchGroup = "true")
	private Experiment experiment;
	
	
	public User(String userid) {
		this.userid = userid;
		this.experiment = new Experiment();
		this.key = generateKeyFromID(userid);
	}
	
	public static Key generateKeyFromID(String userid) {

		return KeyFactory.createKey(User.class.getSimpleName(), "id_" + userid);
	}
	
	public static User getUseridFromCookie(HttpServletRequest req, HttpServletResponse resp) {

		// Get an array of Cookies associated with this domain

		String userid = null;
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals("username")) {
					userid = c.getValue();
					break;
				}
			}
		}

		if (userid == null) {
			userid = UUID.randomUUID().toString();
			;
		}

		Cookie username = new Cookie("username", userid);
		resp.addCookie(username);
		
		User user = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			user = pm.getObjectById(User.class, User.generateKeyFromID(userid));
		} catch (Exception e) {
			user = new User(userid);
			pm.makePersistent(user);
		}
		
		return user;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public boolean getsTreatment(String treatmentName) {
		return this.experiment.getsTreatment(treatmentName);
	}
	
	public Map<String, Boolean> getTreatments() {
		return this.experiment.treatments;
	}
	
}
