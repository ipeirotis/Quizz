package us.quizz.endpoints;

import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.inject.Inject;

import us.quizz.entities.BrowserStats;
import us.quizz.entities.DomainStats;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.service.BrowserStatsService;
import us.quizz.service.DomainStatsService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;
import us.quizz.utils.Constants;
import us.quizz.utils.Security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz",
     description = "The API for Quizz.us",
     version = "v1",
     clientIds = {Constants.PROD_WEB_CLIENT_ID, Constants.PROD_SERVICE_CLIENT_ID,
                  Constants.DEV_WEB_CLIENT_ID, Constants.DEV_SERVICE_CLIENT_ID,
                  API_EXPLORER_CLIENT_ID},
     scopes = {Constants.EMAIL_SCOPE})
public class ReportsEndpoint {
  private QuizService quizService;
  private QuestionService questionService;
  private BrowserStatsService browserStatsService;
  private DomainStatsService domainStatsService;

  @Inject
  public ReportsEndpoint(
      QuizService quizService,
      QuestionService questionService,
      BrowserStatsService browserStatsService,
      DomainStatsService domainStatsService) {
    this.quizService = quizService;
    this.questionService = questionService;
    this.browserStatsService = browserStatsService;
    this.domainStatsService = domainStatsService;
  }

  @ApiMethod(name = "reports.multiChoiceAnswers", path = "reports/multiChoiceAnswers")
  public List<Question> getMultiChoiceAnswersReport(@Named("quizID") String quizID, User user)
      throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    return questionService.getQuizQuestions(quizID);
  }

  @ApiMethod(name = "reports.freeTextAnswers", path = "reports/freeTextAnswers")
  public List<Question> getFreeTextAnswersReport(@Named("quizID") String quizID, User user)
      throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    return questionService.getQuizQuestions(quizID);
  }

  @ApiMethod(name = "reports.scoreByBrowser", path = "reports/scoreByBrowser")
  public List<BrowserStats> getScoreByBrowserReport(User user) throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    return browserStatsService.listAll();
  }

  @ApiMethod(name = "reports.scoreByDomain", path = "reports/scoreByDomain")
  public CollectionResponse<DomainStats> getScoreByDomainReport(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit,
      User user) throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    return domainStatsService.listWithCursor(cursorString, limit);
  }

  @ApiMethod(name = "reports.contributionQuality", path = "reports/contributionQuality")
  public List<Map<String, Object>> getContributionQualityReport(User user)
      throws UnauthorizedException {
    Security.verifyAuthenticatedUser(user);
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    List<Quiz> quizzes = quizService.listAll();

    for (Quiz quiz : quizzes) {
      Map<String, Object> item = new HashMap<String, Object>();

      item.put("quiz", quiz);
      item.put("capacity99", quiz.getCapacity(0.01));
      item.put("capacity95", quiz.getCapacity(0.05));
      item.put("capacity90", quiz.getCapacity(0.10));

      result.add(item);
    }
    return result;
  }
}
