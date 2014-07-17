package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.Experiment;
import us.quizz.entities.User;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.ExperimentRepository;
import us.quizz.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserService extends OfyBaseService<User> {
  public static final String COOKIE_NAME = "username";
  protected static final int COOKIE_MAX_AGE = 60 * 24 * 3600;  // 60 days.
  private ExperimentRepository experimentRepository;

  @Inject
  public UserService(UserRepository userRepository, ExperimentRepository experimentRepository) {
    super(userRepository);
    this.experimentRepository = experimentRepository;
  }

  public String getUseridFromCookie(HttpServletRequest req) {
    return getUseridFromCookie(req, null);
  }

  // Gets the userid stored in the cookie of HttpServletRequest, if any. If there is no
  // corresponding cookie, generate a random userid and returns it. This will also adds a cookie
  // with the corresponding userid in the HttpServletResponse if it is not null.
  // If we generate a new random userid, we will also ASYNCHRONOUSLY save a new User entity for
  // that.
  public String getUseridFromCookie(HttpServletRequest req, HttpServletResponse resp) {
    // Get an array of Cookies associated with this domain
    String userid = null;
    Cookie[] cookies = req.getCookies();
    // This will always be null, see bug tracker here:
    // https://code.google.com/p/googleappengine/issues/detail?id=10100
    if (cookies != null) {
      for (Cookie c : cookies) {
        if (c.getName().equals(COOKIE_NAME)) {
          userid = c.getValue();
          break;
        }
      }
    }

    if (userid != null) {
      // If there is no associated User in the datastore for this userid, there are two reasons:
      // - This user visited Quizz before and somehow its User entity was deleted from datastore
      //   during previous migration etc.
      // - The userid in the cookie might be faked by a malicious user.
      // So, we are just going to reset and create a new userid.
      if (get(userid) == null) {
        userid = null;
      }
    }

    if (userid == null) {
      userid = UUID.randomUUID().toString();
      asyncSave(new User(userid));
    }

    if (resp != null) {
      Cookie username = new Cookie(COOKIE_NAME, userid);
      username.setMaxAge(COOKIE_MAX_AGE);
      username.setPath("/");
      resp.addCookie(username);
    }
    return userid;
  }

  // Gets a new User entity for the given userid, or if it does not exist, create a new one
  // and store it in datastore.
  public User getOrCreateUser(String userid) {
    User user = get(userid);
    if (user == null) {
      user = new User(userid);
      save(user);
    }
    return user;
  }

  // Same as getUseridFromCookie, but returns the User entity instead of just the userid.
  public User getUserFromCookie(HttpServletRequest req, HttpServletResponse resp) {
    String userid = getUseridFromCookie(req, resp);
    flush();
    return getOrCreateUser(userid);
  }
}
