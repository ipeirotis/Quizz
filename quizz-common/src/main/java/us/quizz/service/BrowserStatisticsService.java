package us.quizz.service;

import nl.bitwalker.useragentutils.Browser;
import us.quizz.entities.BrowserStats;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.service.UserReferralService.Result;

import com.google.inject.Inject;

public class BrowserStatisticsService {
  private BrowserStatsRepository browserStatsRepository;
  private QuizPerformanceRepository quizPerformanceRepository;
  private UserReferralService userReferralService;

  @Inject
  public BrowserStatisticsService(
      QuizPerformanceRepository quizPerformanceRepository,
      UserReferralService userReferralService,
      BrowserStatsRepository browserStatsRepository) {
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.userReferralService = userReferralService;
    this.browserStatsRepository = browserStatsRepository;
  }
  
  public void updateStatistics(String browser) {
    Browser b = Browser.valueOf(browser);
    Result res = userReferralService.getCountByBrowser(b);

    Double userScores = quizPerformanceRepository.getScoreSumByIds(res.getUsers());
    if (res.getCount() > 0) {
      BrowserStats bs = new BrowserStats(b, res.getCount(), userScores);
      browserStatsRepository.save(bs);
    }
  }
}
