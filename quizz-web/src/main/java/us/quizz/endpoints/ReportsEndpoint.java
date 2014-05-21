package us.quizz.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.inject.Inject;

import us.quizz.entities.BrowserStats;
import us.quizz.entities.DomainStats;
import us.quizz.entities.Question;
import us.quizz.entities.Quiz;
import us.quizz.service.BrowserStatsService;
import us.quizz.service.DomainStatsService;
import us.quizz.service.QuestionService;
import us.quizz.service.QuizService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "quizz", description = "The API for Quizz.us", version = "v1")
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
  public List<Question> getMultiChoiceAnswersReport(@Named("quizID")String quizID) {
    return questionService.getQuizQuestions(quizID);
  }

  @ApiMethod(name = "reports.freeTextAnswers", path = "reports/freeTextAnswers")
  public List<Question> getFreeTextAnswersReport(@Named("quizID")String quizID) {
    return questionService.getQuizQuestions(quizID);
  }

  @ApiMethod(name = "reports.scoreByBrowser", path = "reports/scoreByBrowser")
  public List<BrowserStats> getScoreByBrowserReport() {
    return browserStatsService.list();
  }

  @ApiMethod(name = "reports.scoreByDomain", path = "reports/scoreByDomain")
  public CollectionResponse<DomainStats> getScoreByDomainReport(
      @Nullable @Named("cursor") String cursorString,
      @Nullable @Named("limit") Integer limit) {
    return domainStatsService.listWithCursor(cursorString, limit);
  }

  @ApiMethod(name = "reports.contributionQuality", path = "reports/contributionQuality")
  public List<Map<String, Object>> getContributionQualityReport() {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    List<Quiz> quizzes = quizService.list();

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
