package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
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

  // Lists the QuizPerformance in the datastore using paging support.
  @ApiMethod(name = "listQuizPerformance", path = "listQuizPerformance")
  public CollectionResponse<QuizPerformance> listQuizPerformance(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    return quizPerformanceService.listWithCursor(cursorString, limit);
  }

  // Lists all the QuizPerformance of the relevant user.
  @ApiMethod(name = "listQuizPerformanceByUser", path = "listQuizPerformanceByUser",
             httpMethod = HttpMethod.POST)
  public CollectionResponse<QuizPerformance> listQuizPerformanceByUser(
      @Named("userid") String userid) {
    List<QuizPerformance> execute = quizPerformanceService.getQuizPerformancesByUser(userid);
    return CollectionResponse.<QuizPerformance> builder().setItems(execute).build();
  }

  // Gets the QuizPerformance of the given quizID and userID.
  @ApiMethod(name = "getQuizPerformance", path = "getQuizPerformance", httpMethod = HttpMethod.POST)
  public QuizPerformance getQuizPerformance(
      @Named("quizID") String quizID, @Named("userID") String userID) {
    QuizPerformance quizperformance = quizPerformanceService.getNoCache(quizID, userID);
    if (quizperformance == null) {
      quizperformance = new QuizPerformance(quizID, userID);
    }
    return quizperformance;
  }
}
