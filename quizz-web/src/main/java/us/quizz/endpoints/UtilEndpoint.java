package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.inject.Inject;

import us.quizz.entities.UserReferal;
import us.quizz.repository.UserReferralRepository;
import us.quizz.utils.UrlUtils;

import java.util.List;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class UtilEndpoint {
  @Inject
  private UserReferralRepository userReferralRepository;

  @ApiMethod(name = "util.resaveUserReferals", path = "util/resaveUserReferals")
  public void resaveUserReferals() {
    List<UserReferal> list = userReferralRepository.listAllByCursor();
    for (UserReferal ref : list) {
      if (ref.getReferer() != null) {
        ref.setDomain(UrlUtils.extractDomain(ref.getReferer().getValue()));
      }
    }
    userReferralRepository.saveAll(list);
  } 
}
