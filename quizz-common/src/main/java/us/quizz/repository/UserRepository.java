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
}
