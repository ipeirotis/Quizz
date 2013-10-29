package com.ipeirotis.crowdquiz.entities;

import java.util.ArrayList;
import java.util.List;
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

import javax.jdo.Query;

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
	private String fbid;
	
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
	
	public static User getOrCreate(String userid){
		User user = PMF.singleGetObjectById(User.class, User.generateKeyFromID(userid));
		if (user == null) {
			user = new User(userid);
			Experiment exp = new Experiment();
			exp.assignTreatments();
			user.setExperiment(exp);
			PMF.singleMakePersistent(user);
		}
		return user;
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
		}

		Cookie username = new Cookie("username", userid);
		username.setMaxAge(60 * 24 * 3600);
		username.setPath("/");
		resp.addCookie(username);
		
		return getOrCreate(userid);
	}

	public static User getUseridFromFbid(String fbid) {
		System.out.println("start get pm");
		PersistenceManager pm = PMF.getPM();
		User user = null;
		System.out.println("try to get user");
		try {
			System.out.println("1");
			Query query = pm.newQuery(User.class);
			query.setFilter("fbid == fbidParam");
			query.declareParameters("String fbidParam");
			System.out.println("2");
			
			@SuppressWarnings("unchecked")
			List<User> users = (List<User>) query.execute(fbid);
			user = users.get(0);
			System.out.println("3");
		} catch (Exception e) {
			System.out.println("cannot get user");
		} finally {
			pm.close();
		}
		return user;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public String getFBID() {
		return fbid;
	}
	
	public void setFBID(String fbid) {
		this.fbid = fbid;
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
