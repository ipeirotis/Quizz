package us.quizz.endpoints;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import us.quizz.entities.DomainStats;
import us.quizz.entities.UserReferal;
import us.quizz.repository.DomainStatsRepository;
import us.quizz.service.UserReferralService;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class UserReferalEndpoint {
  private UserReferralService userReferalService;
  private DomainStatsRepository domainStatsRepository;

  @Inject
  public UserReferalEndpoint(
      UserReferralService userReferalService,
      DomainStatsRepository domainStatsRepository) {
    this.userReferalService = userReferalService;
    this.domainStatsRepository = domainStatsRepository;
  }

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP
   * GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   *         persisted and a cursor to the next page.
   */
  @ApiMethod(name = "listUserReferal")
  public CollectionResponse<UserReferal> listUserReferal(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    return userReferalService.listWithCursor(cursorString, limit);
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET
   * method.
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  @ApiMethod(name = "getUserReferal")
  public UserReferal getUserReferal(@Named("id") Long id) {
    return userReferalService.get(id);
  }

  /**
   * This inserts a new entity into App Engine datastore. If the entity
   * already exists in the datastore, an exception is thrown. It uses HTTP
   * POST method.
   * @param userreferal the entity to be inserted.
   * @return The inserted entity.
   */
  @ApiMethod(name = "insertUserReferal")
  public UserReferal insertUserReferal(UserReferal userreferal) {
    return userReferalService.save(userreferal);
  }

  /**
   * This method is used for updating an existing entity. If the entity does
   * not exist in the datastore, an exception is thrown. It uses HTTP PUT method.
   * @param userreferal the entity to be updated.
   * @return The updated entity.
   */
  @ApiMethod(name = "updateUserReferal")
  public UserReferal updateUserReferal(UserReferal userreferal) {
    return userReferalService.save(userreferal);
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   */
  @ApiMethod(name = "removeUserReferal")
  public void removeUserReferal(@Named("id") Long id) {
    userReferalService.delete(id);
  }

  @ApiMethod(name = "listDomains", path = "listDomains")
  public List<DomainStats> listDomains() {
    return domainStatsRepository.list();
  }
}
