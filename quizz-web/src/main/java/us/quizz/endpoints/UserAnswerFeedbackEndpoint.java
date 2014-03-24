package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import us.quizz.entities.UserAnswerFeedback;
import us.quizz.repository.UserAnswerFeedbackRepository;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1",
     namespace = @ApiNamespace(ownerDomain = "crowd-power.appspot.com",
                               ownerName = "crowd-power.appspot.com",
                               packagePath = "us.quizz.endpoints"))
public class UserAnswerFeedbackEndpoint {
  private UserAnswerFeedbackRepository userAnswerFeedbackRepository;

  @Inject
  public UserAnswerFeedbackEndpoint(UserAnswerFeedbackRepository userAnswerFeedbackRepository) {
    this.userAnswerFeedbackRepository = userAnswerFeedbackRepository;
  }

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP
   * GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   *         persisted and a cursor to the next page.
   */
  @ApiMethod(name = "listUserAnswerFeedback")
  public CollectionResponse<UserAnswerFeedback> listUserAnswerFeedback(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    return userAnswerFeedbackRepository.listItems(cursorString, limit);
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET
   * method.
   *
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  @ApiMethod(name = "getUserAnswerFeedback")
  public UserAnswerFeedback getUserAnswerFeedback(
      @Named("question") Long questionID, @Named("userid") String userid) {
    return userAnswerFeedbackRepository.singleGetObjectByIdThrowing(
        UserAnswerFeedback.generateKeyFromID(questionID, userid));
  }

  /**
   * This inserts a new entity into App Engine datastore. If the entity
   * already exists in the datastore, an exception is thrown. It uses HTTP
   * POST method.
   * @param useranswerfeedback the entity to be inserted.
   * @return The inserted entity.
   */
  @ApiMethod(name = "insertUserAnswerFeedback")
  public UserAnswerFeedback insertUserAnswerFeedback(
      UserAnswerFeedback useranswerfeedback) {
    return userAnswerFeedbackRepository.insert(useranswerfeedback);
  }

  /**
   * This method is used for updating an existing entity. If the entity does
   * not exist in the datastore, an exception is thrown. It uses HTTP PUT
   * method.
   * @param useranswerfeedback the entity to be updated.
   * @return The updated entity.
   */
  @ApiMethod(name = "updateUserAnswerFeedback")
  public UserAnswerFeedback updateUserAnswerFeedback(
      UserAnswerFeedback useranswerfeedback) {
    return userAnswerFeedbackRepository.update(useranswerfeedback);
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   */
  @ApiMethod(name = "removeUserAnswerFeedback")
  public void removeUserAnswerFeedback(@Named("id") Long id) {
    UserAnswerFeedback uaf = userAnswerFeedbackRepository.singleGetObjectByIdThrowing(id);
    userAnswerFeedbackRepository.remove(uaf.getKey());
  }
}
