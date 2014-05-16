package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import us.quizz.entities.Experiment;
import us.quizz.entities.User;
import us.quizz.service.ExperimentService;
import us.quizz.service.UserReferralService;
import us.quizz.service.UserService;
import us.quizz.utils.ChannelHelpers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class UserEndpoint {
  private UserService userService;
  private ExperimentService experimentService;
  private UserReferralService userReferralService;

  @Inject
  public UserEndpoint(UserService userService,
      UserReferralService userReferralService, ExperimentService experimentService) {
    this.userService = userService;
    this.userReferralService = userReferralService;
    this.experimentService = experimentService;
  }

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP
   * GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   *         persisted and a cursor to the next page.
   */
  @ApiMethod(name = "listUser", path = "user/list")
  public CollectionResponse<User> listUser(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    return userService.listWithCursor(cursorString, limit);
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET
   * method.
   *
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  @ApiMethod(name = "getUser", path = "user")
  public Map<String, Object> getUser(HttpServletRequest req, @Named("userid") String userid) {
    User user = userService.getOrCreateUser(userid);

    userReferralService.createAndStoreUserReferal(req, userid);

    Experiment e = experimentService.get(user.getExperimentId());
    if (e != null && e.getTreatments() != null) {
      for (String s : e.getTreatments().keySet()) {
        e.getTreatments().get(s);
      }
    }

    Map<String, Object> result = new HashMap<String, Object>();
    result.put("user", user);
    result.put("token", ChannelHelpers.createChannel(userid));
    return result;
  }

  /**
   * This inserts a new entity into App Engine datastore. If the entity
   * already exists in the datastore, an exception is thrown. It uses HTTP
   * POST method.
   * @param user the entity to be inserted.
   * @return The inserted entity.
   */
  @ApiMethod(name = "insertUser")
  public User insertUser(User user) {
    return userService.save(user);
  }

  /**
   * This method is used for updating an existing entity. If the entity does
   * not exist in the datastore, an exception is thrown. It uses HTTP PUT
   * method.
   * @param user the entity to be updated.
   * @return The updated entity.
   */
  @ApiMethod(name = "updateUser")
  public User updateUser(User user) {
    return userService.save(user);
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE
   * method.
   * @param id the primary key of the entity to be deleted.
   */
  @ApiMethod(name = "removeUser")
  public void removeUser(@Named("userid") String userid) {
    userService.delete(userid);
  }

  @ApiMethod(name = "updateUserExperiment", path = "updateUserExperiment")
  public void updateUserExperiment(@Named("userid") String userid) {
    User user = userService.get(userid);
    if(user == null) {
      user = new User(userid);
    }
    Experiment exp = new Experiment();
    exp = experimentService.save(exp);
    user.setExperimentId(exp.getId());
    userService.save(user);
  }
}
