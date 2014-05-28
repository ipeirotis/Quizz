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
  protected static final String COOKIE_NAME = "username";
  protected static final int COOKIE_MAX_AGE = 60 * 24 * 3600;  // 60 days.
  private ExperimentRepository experimentRepository;

  @Inject
  public UserService(UserRepository userRepository, ExperimentRepository experimentRepository) {
    super(userRepository);
    this.experimentRepository = experimentRepository;
  }

  public User getOrCreateUser(String userid) {
    User user = get(userid);
    if (user == null) {
      user = new User(userid);
      Experiment exp = new Experiment();
      exp = experimentRepository.saveAndGet(exp);
      user.setExperimentId(exp.getId());
      save(user);
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
        if (c.getName().equals(COOKIE_NAME)) {
          userid = c.getValue();
          break;
        }
      }
    }

    if (userid == null) {
      userid = UUID.randomUUID().toString();
    }

    if (resp != null) {
      Cookie username = new Cookie(COOKIE_NAME, userid);
      username.setMaxAge(COOKIE_MAX_AGE);
      username.setPath("/");
      resp.addCookie(username);
    }

    return getOrCreateUser(userid);
  }
}
