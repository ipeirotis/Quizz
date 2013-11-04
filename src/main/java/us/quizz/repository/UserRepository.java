package us.quizz.repository;

import java.util.List;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipeirotis.crowdquiz.entities.Experiment;
import com.ipeirotis.crowdquiz.entities.User;
import com.ipeirotis.crowdquiz.utils.PMF;

public class UserRepository {
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

	public static User getUseridFromSocialid(String fbid) {
		PersistenceManager pm = PMF.getPM();
		User user;
		try {
			Query query = pm.newQuery(User.class);
			query.setFilter("fbid == fbidParam");
			query.declareParameters("String fbidParam");
			
			@SuppressWarnings("unchecked")
			List<User> users = (List<User>) query.execute(fbid);
			user = users.get(0);
		} catch (Exception e) {
			user = null;
		} finally {
			pm.close();
		}
		return user;
	}
}
