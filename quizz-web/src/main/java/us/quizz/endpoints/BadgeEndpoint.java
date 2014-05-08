package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.inject.Inject;

import us.quizz.entities.Badge;
import us.quizz.repository.BadgeRepository;

import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class BadgeEndpoint {
  private BadgeRepository badgeRepository;

  @Inject
  public BadgeEndpoint(BadgeRepository badgeRepository) {
    this.badgeRepository = badgeRepository;
  }

  @ApiMethod(name = "addBadge", path="addBadge", httpMethod=HttpMethod.POST)
  public void addBadge(@Named("name") String name, @Named("sname") String sname) {
    Badge badge = new Badge(name, sname);
    badgeRepository.singleMakePersistent(badge);
  }
}
