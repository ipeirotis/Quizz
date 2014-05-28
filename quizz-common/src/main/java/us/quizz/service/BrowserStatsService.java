package us.quizz.service;

import com.google.inject.Inject;

import nl.bitwalker.useragentutils.Browser;

import us.quizz.entities.BrowserStats;
import us.quizz.ofy.OfyBaseService;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.service.UserReferralService.Result;

import java.util.List;

public class BrowserStatsService extends OfyBaseService<BrowserStats> {
  private QuizPerformanceService quizPerformanceService;
  private UserReferralService userReferralService;

  @Inject
  public BrowserStatsService(
      QuizPerformanceService quizPerformanceService,
      UserReferralService userReferralService,
      BrowserStatsRepository browserStatsRepository) {
    super(browserStatsRepository);
    this.quizPerformanceService = quizPerformanceService;
    this.userReferralService = userReferralService;
  }

  public void updateStatistics(String browser) {
    Browser b = Browser.valueOf(browser);
    Result res = userReferralService.getCountByBrowser(b);
    if (res.getCount() > 0) {
      Double userScores = quizPerformanceService.getScoreSumByIds(res.getQuizPerformanceIds());
      save(new BrowserStats(b, res.getCount(), userScores));
    }
  }
}
