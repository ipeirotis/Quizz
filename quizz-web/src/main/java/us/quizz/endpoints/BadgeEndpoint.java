package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.inject.Inject;

import us.quizz.entities.Badge;
import us.quizz.repository.BadgeRepository;
import us.quizz.service.BadgeService;

import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class BadgeEndpoint {
  private BadgeService badgeService;

  @Inject
  public BadgeEndpoint(BadgeService badgeService) {
    this.badgeService = badgeService;
  }

  @ApiMethod(name = "addBadge", path="addBadge", httpMethod=HttpMethod.POST)
  public void addBadge(@Named("name") String name, @Named("sname") String sname) {
    Badge badge = new Badge(name, sname);
    badgeService.save(badge);
  }
}
