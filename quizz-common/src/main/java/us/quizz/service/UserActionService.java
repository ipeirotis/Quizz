package us.quizz.service;

import com.google.inject.Inject;

import us.quizz.entities.UserAction;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.UserActionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Interface for getting user actions. */
public class UserActionService extends OfyBaseService<UserAction> {
  @Inject
  public UserActionService(UserActionRepository userActionRepository){
    super(userActionRepository);
  }

  public List<UserAction> list(String userId) {
    Map<String, Object> params = new HashMap<String, Object>();
    if (userId != null) {
      params.put("userid", userId);
    }
    return listAll(params);
  }

  public List<UserAction> getUserActions(String userid) {
    return ((UserActionRepository) baseRepository).getUserActions(userid);
  }
}
