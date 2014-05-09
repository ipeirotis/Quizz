package us.quizz.repository;

import us.quizz.entities.UserReferal;
import us.quizz.ofy.OfyBaseRepository;

public class UserReferralRepository extends OfyBaseRepository<UserReferal>{
  public UserReferralRepository() {
    super(UserReferal.class);
  }
}
