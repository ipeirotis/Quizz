package us.quizz.repository;

import us.quizz.entities.UserAction;
import us.quizz.ofy.OfyBaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Repository for user actions.us */
public class UserActionRepository extends OfyBaseRepository<UserAction> {
  public UserActionRepository() {
    super(UserAction.class);
  }

  public List<UserAction> getUserActions(String userid) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("userid", userid);
    return listAllByCursor(params);
  }
}
