package us.quizz.repository;

import com.google.appengine.api.datastore.Key;

import us.quizz.entities.Experiment;
import us.quizz.entities.User;
import us.quizz.entities.UserAnswer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserRepository extends BaseRepository<User> {
  public UserRepository() {
    super(User.class);
  }

  @Override
  protected Key getKey(User item) {
    return item.getKey();
  }

  @Override
  public void fetchItem(User user) {
    user.getExperiment();
    user.getTreatments();
  }

  public User get(String id) {
    return singleGetObjectById(User.generateKeyFromID(id));
  }

  public Set<String> getUserIDs(String quiz) {
    PersistenceManager pm = getPersistenceManager();
    try {
      Query q = pm.newQuery(UserAnswer.class);
      q.setFilter("quizID == quizParam");
      q.declareParameters("String quizParam");

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("quizParam", quiz);

      Set<String> answers = new TreeSet<String>();
      int limit = 1000;
      int i = 0;
      while (true) {
        q.setRange(i, i + limit);
        @SuppressWarnings("unchecked")
        List<UserAnswer> results = (List<UserAnswer>) q.executeWithMap(params);
        if (results.size() == 0) {
          break;
        }
        for (UserAnswer ua : results) {
          answers.add(ua.getUserid());
        }
        i += limit;
      }
      return answers;
    } finally {
      pm.close();
    }
  }

  public User getOrCreate(String userid) {
    User user = singleGetObjectById(User.class, User.generateKeyFromID(userid));
    if (user == null) {
      user = new User(userid);
      Experiment exp = new Experiment();
      exp.assignTreatments();
      user.setExperiment(exp);
      singleMakePersistent(user);
    }
    return user;
  }

  public User getUseridFromCookie(HttpServletRequest req) {
    return getUseridFromCookie(req, null);
  }

  public User getUseridFromCookie(HttpServletRequest req, HttpServletResponse resp) {
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

    if(resp != null) {
      Cookie username = new Cookie("username", userid);
      username.setMaxAge(60 * 24 * 3600);
      username.setPath("/");
      resp.addCookie(username);
    }

    return getOrCreate(userid);
  }

  public User getUseridFromSocialid(String fbid) {
    PersistenceManager pm = getPersistenceManager();
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
