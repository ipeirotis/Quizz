package us.quizz.endpoints;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.repository.QuizQuestionRepository;
import us.quizz.service.QuizService;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1",
     namespace = @ApiNamespace(ownerDomain = "crowd-power.appspot.com",
                               ownerName = "crowd-power.appspot.com",
                               packagePath = "us.quizz.endpoints"))
public class QuizEndpoint {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(QuizEndpoint.class.getName());
  
  protected static int QUESTION_PACKAGE_SIZE = 10;

  private QuizService quizService;
  private QuizQuestionRepository quizQuestionRepository;

  @Inject
  public QuizEndpoint(QuizService quizService, QuizQuestionRepository quizQuestionRepository) {
    this.quizService = quizService;
    this.quizQuestionRepository = quizQuestionRepository;
  }

  /**
   * This method lists all the entities inserted in datastore. It uses HTTP
   * GET method and paging support.
   *
   * @return A CollectionResponse class containing the list of all entities
   *         persisted and a cursor to the next page.
   */
  @ApiMethod(name = "listQuiz", path = "listQuiz", httpMethod = HttpMethod.GET)
  public CollectionResponse<Quiz> listQuiz(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    List<Quiz> list = quizService.list();
    return CollectionResponse.<Quiz> builder().setItems(list)
        .setNextPageToken(cursorString).build();
  }

  /**
   * This method gets the entity having primary key id. It uses HTTP GET
   * method.
   * 
   * @param id
   *            the primary key of the java bean.
   * @return The entity with primary key id.
   */
  @ApiMethod(name = "getQuiz", path = "getQuiz", httpMethod = HttpMethod.GET)
  public Quiz getQuiz(@Named("id") String id) {
    return quizService.get(id);
  }

  // Sets the quiz for the quizID given to be shown on landing page by default.
  @ApiMethod(name = "showQuiz", path = "showQuiz", httpMethod = HttpMethod.GET)
  public Quiz showQuiz(@Named("quizID") String quizID) {
    Quiz quiz = quizService.get(quizID);
    quiz.setShowOnDefault(true);
    return quizService.save(quiz);
  }

  // Sets the quiz for the quizID given to be hidden on landing page by default.
  @ApiMethod(name = "hideQuiz", path = "hideQuiz", httpMethod = HttpMethod.GET)
  public Quiz hideQuiz(@Named("quizID") String quizID) {
    Quiz quiz = quizService.get(quizID);
    quiz.setShowOnDefault(false);
    return quizService.save(quiz);
  }

  /**
   * This method generates questions for quiz.
   */
  @ApiMethod(name = "listNextQuestions", path = "listNextQuestions",
             httpMethod = HttpMethod.POST)
  public Map<String, Set<Question>> getNextQuestions(
      @Named("quizID") String quizID,
      @Named("userID") String userID,
      @Nullable @Named("num") Integer num) {
    if (num == null) {
      num = QUESTION_PACKAGE_SIZE;
    }
    return quizQuestionRepository.getNextQuizQuestions(quizID, num, userID);
  }

  /**
   * This inserts a new entity into App Engine datastore. If the entity
   * already exists in the datastore, an exception is thrown. It uses HTTP
   * POST method.
   * 
   * @param quiz
   *            the entity to be inserted.
   * @return The inserted entity.
   */
  @ApiMethod(name = "insertQuiz", path = "insertQuiz", httpMethod = HttpMethod.POST)
  public Quiz insertQuiz(Quiz quiz) {
    if (quiz.getShowOnDefault() == null) {
      quiz.setShowOnDefault(false);
    }
    return quizService.save(quiz);
  }

  /**
   * This method is used for updating an existing entity. If the entity does
   * not exist in the datastore, an exception is thrown. It uses HTTP PUT
   * method.
   * 
   * @param quiz
   *            the entity to be updated.
   * @return The updated entity.
   */
  @ApiMethod(name = "updateQuiz", path = "updateQuiz", httpMethod = HttpMethod.PUT)
  public Quiz updateQuiz(Quiz quiz) {
    return quizService.save(quiz);
  }

  /**
   * This method removes the entity with primary key id. It uses HTTP DELETE
   * method.
   * 
   * @param id the primary key of the entity to be deleted.
   */
  @ApiMethod(name = "removeQuiz", path = "removeQuiz", httpMethod = HttpMethod.DELETE)
  public void removeQuiz(@Named("id") String id) {
    quizService.delete(id);
  }
}
