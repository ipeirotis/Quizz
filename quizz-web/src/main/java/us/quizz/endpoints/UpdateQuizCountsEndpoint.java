package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.inject.Inject;

import us.quizz.service.QuizService;

import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
public class UpdateQuizCountsEndpoint {
  private QuizService quizService;

  @Inject
  public UpdateQuizCountsEndpoint(QuizService quizService) {
    this.quizService = quizService;
  }

  @ApiMethod(name = "updateQuizCounts", path="updateQuizCounts")
  public void getQuizCounts(@Named("quizID") String quizID) {
    quizService.updateQuizCounts(quizID);
  }
}
