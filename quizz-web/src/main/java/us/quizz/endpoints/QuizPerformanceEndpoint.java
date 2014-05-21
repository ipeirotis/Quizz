package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import us.quizz.entities.QuizPerformance;
import us.quizz.service.QuizPerformanceService;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class QuizPerformanceEndpoint {
  private QuizPerformanceService quizPerformanceService;

  @Inject
  public QuizPerformanceEndpoint(QuizPerformanceService quizPerformanceService) {
    this.quizPerformanceService = quizPerformanceService;
  }

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP
   * GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   *         persisted and a cursor to the next page.
   */
  @ApiMethod(name = "listQuizPerformance")
  public CollectionResponse<QuizPerformance> listQuizPerformance(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    return quizPerformanceService.listWithCursor(cursorString, limit);
  }

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP
   * GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   *         persisted and a cursor to the next page.
   */
  @ApiMethod(name = "listQuizPerformanceByUser", path = "quizperformance/user/{user}")
  public CollectionResponse<QuizPerformance> listQuizPerformanceByUser(
      @Named("user") String userid) {
    List<QuizPerformance> execute = quizPerformanceService
        .getQuizPerformancesByUser(userid);
    return CollectionResponse.<QuizPerformance> builder().setItems(execute).build();
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET method.
   *
   * @param id the primary key of the java bean.
   * @return The entity with primary key id.
   */
  @ApiMethod(name = "getQuizPerformance", path = "quizperformance/quiz/{quiz}/user/{user}")
  public QuizPerformance getQuizPerformance(@Named("quiz") String quiz,
      @Named("user") String userid) {
    QuizPerformance quizperformance = quizPerformanceService.get(quiz, userid);
    if (quizperformance == null) {
      quizperformance = new QuizPerformance(quiz, userid);
    }
    return quizperformance;
  }

  /**
   * This inserts a new entity into App Engine datastore. If the entity
   * already exists in the datastore, an exception is thrown. It uses HTTP
   * POST method.
   * 
   * @param quizperformance the entity to be inserted.
   * @return The inserted entity.
   */
  @ApiMethod(name = "insertQuizPerformance")
  public QuizPerformance insertQuizPerformance(QuizPerformance quizperformance) {
    return quizPerformanceService.save(quizperformance);
  }

  /**
   * This method is used for updating an existing entity. If the entity does
   * not exist in the datastore, an exception is thrown. It uses HTTP PUT
   * method.
   * 
   * @param quizperformance the entity to be updated.
   * @return The updated entity.
   */
  @ApiMethod(name = "updateQuizPerformance")
  public QuizPerformance updateQuizPerformance(QuizPerformance quizperformance) {
    return quizPerformanceService.save(quizperformance);
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE method.
   *
   * @param id the primary key of the entity to be deleted.
   */
  @ApiMethod(name = "removeQuizPerformance")
  public void removeQuizPerformance(@Named("quizid") String quiz,
      @Named("userid") String userid) {
    quizPerformanceService.delete(quiz, userid);
  }
}
