package us.quizz.service;

import com.google.inject.Inject;

import eu.bitwalker.useragentutils.Browser;

import us.quizz.entities.BrowserStats;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.repository.QuizPerformanceRepository;
import us.quizz.repository.UserReferralRepository;
import us.quizz.repository.UserReferralRepository.Result;

public class BrowserStatisticsService {
  private BrowserStatsRepository browserStatsRepository;
  private QuizPerformanceRepository quizPerformanceRepository;
  private UserReferralRepository userReferralRepository;

  @Inject
  public BrowserStatisticsService(
      QuizPerformanceRepository quizPerformanceRepository,
      UserReferralRepository userReferralRepository,
      BrowserStatsRepository browserStatsRepository) {
    this.quizPerformanceRepository = quizPerformanceRepository;
    this.userReferralRepository = userReferralRepository;
    this.browserStatsRepository = browserStatsRepository;
  }
  
  public void updateStatistics(String browser) {
    Browser b = Browser.valueOf(browser);
    Result res = userReferralRepository.getCountByBrowser(b);

    Double userScores = quizPerformanceRepository.getScoreSumByIds(res.getUsers());
    if (res.getCount() > 0) {
      BrowserStats bs = new BrowserStats(b, res.getCount(), userScores);
      browserStatsRepository.save(bs);
    }
  }
}
