package us.quizz.service;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import us.quizz.entities.Experiment;
import us.quizz.entities.User;
import us.quizz.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserService {
  private UserRepository userRepository;
  private ExperimentService experimentService;

  @Inject
  public UserService(UserRepository userRepository, ExperimentService experimentService){
    this.userRepository = userRepository;
    this.experimentService = experimentService;
  }

  public List<User> listAll(){
    return userRepository.listAllByCursor();
  }

  public CollectionResponse<User> listWithCursor(String cursor, Integer limit){
    return userRepository.listByCursor(cursor, limit);
  }

  public User get(String id) {
    return userRepository.get(id);
  }

  public User save(User user) {
    return userRepository.saveAndGet(user);
  }

  public void delete(String id) {
    userRepository.delete(id);
  }

  public User getOrCreateUser(String userid) {
    User user = userRepository.get(userid);
    if (user == null) {
      user = new User(userid);
      Experiment exp = new Experiment();
      exp = experimentService.save(exp);
      user.setExperimentId(exp.getId());
      userRepository.save(user);
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
