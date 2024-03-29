package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.inject.Inject;

import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;
import us.quizz.utils.Constants;
import us.quizz.utils.Security;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz",
     description = "The API for Quizz.us",
     version = "v1",
     clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
                  Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
                  API_EXPLORER_CLIENT_ID},
     scopes = {Constants.EMAIL_SCOPE})
public class QuizEndpoint {
  private QuizService quizService;
  private QuestionService questionService;

  @Inject
  public QuizEndpoint(QuizService quizService, QuestionService questionService) {
    this.quizService = quizService;
    this.questionService = questionService;
  }

  // Gets the quiz by quizID
  @ApiMethod(name = "getQuiz", path = "getQuiz", httpMethod = HttpMethod.GET)
  public Quiz getQuiz(@Named("quizID") String quizID) {
    return quizService.get(quizID);
  }

  // Lists the quiz in datastore using paging.
  // @return A CollectionResponse class containing the list of all entities
  //         persisted and a cursor to the next page.
  @ApiMethod(name = "listQuiz", path = "listQuiz", httpMethod = HttpMethod.GET)
  public CollectionResponse<Quiz> listQuiz(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    return quizService.listWithCursor(cursorString, limit);
  }

  // Sets the quiz for the quizID given to be shown on landing page by default.
  @ApiMethod(name = "showQuiz", path = "showQuiz", httpMethod = HttpMethod.GET)
  public Quiz showQuiz(@Named("quizID") String quizID, User user) throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);

    Quiz quiz = quizService.get(quizID);
    quiz.setShowOnDefault(true);
    return quizService.save(quiz);
  }

  // Sets the quiz for the quizID given to be hidden on landing page by default.
  @ApiMethod(name = "hideQuiz", path = "hideQuiz", httpMethod = HttpMethod.GET)
  public Quiz hideQuiz(@Named("quizID") String quizID, User user) throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);

    Quiz quiz = quizService.get(quizID);
    quiz.setShowOnDefault(false);
    return quizService.save(quiz);
  }

  /**
   * Generates a list of calibration and collection questions for the given userID and quizID.
   *
   * @param quizID quiz to fetch the questions with.
   * @param userID userid for the user to fetch the questions for.
   * @return A map with three items:
   *     CALIBRATION_KEY: A set of calibration questions.
   *     COLLECTION_KEY: A set of collection questions.
   *     NUM_QUESTIONS_KEY: Number of questions to group into a quiz. -1 if there is no limit.
   */
  @ApiMethod(name = "listNextQuestions", path = "listNextQuestions",
             httpMethod = HttpMethod.POST)
  public Map<String, Object> getNextQuestions(
      @Named("quizID") String quizID,
      @Nullable @Named("firstQuestionID") Long firstQuestionID,
      @Named("userID") String userID,
      @Nullable @Named("num") Integer num) {
    return questionService.getNextQuizQuestions(quizID, firstQuestionID, userID);
  }

  // Inserts a new entity into Datastore. If the entity already exists, an exception will be thrown.
  // If the showOnDefault field is not filled, it will be set to false.
  @ApiMethod(name = "insertQuiz", path = "insertQuiz", httpMethod = HttpMethod.POST)
  public Quiz insertQuiz(Quiz quiz, User user) throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);

    if (quiz.getShowOnDefault() == null) {
      quiz.setShowOnDefault(false);
    }
    return quizService.save(quiz);
  }

  // Removes Quiz with the given quizID and all other entities associated with this quiz
  // such as Question, Answer, and UserAnswer.
  @ApiMethod(name = "removeQuizRecursively", path = "removeQuizRecursively",
             httpMethod = HttpMethod.DELETE)
  public void removeQuizRecursively(@Named("quizID") String quizID, User user)
      throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    quizService.deleteRecursively(quizID);
  }

}
