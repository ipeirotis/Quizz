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

  public User getOrCreateUser(String userid) {
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

    return getOrCreateUser(userid);
  }
}
