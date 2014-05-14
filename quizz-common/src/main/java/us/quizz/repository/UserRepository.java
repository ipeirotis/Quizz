package us.quizz.repository;

import us.quizz.entities.User;
import us.quizz.ofy.OfyBaseRepository;

public class UserRepository extends OfyBaseRepository<User> {
  public UserRepository() {
    super(User.class);
  }
}
