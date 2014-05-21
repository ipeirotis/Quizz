package us.quizz.service;

import com.google.inject.Inject;

import nl.bitwalker.useragentutils.Browser;

import us.quizz.entities.BrowserStats;
import us.quizz.repository.BrowserStatsRepository;
import us.quizz.service.UserReferralService.Result;

import java.util.List;

public class BrowserStatsService {
  private BrowserStatsRepository browserStatsRepository;
  private QuizPerformanceService quizPerformanceService;
  private UserReferralService userReferralService;

  @Inject
  public BrowserStatsService(
      QuizPerformanceService quizPerformanceService,
      UserReferralService userReferralService,
      BrowserStatsRepository browserStatsRepository) {
    this.quizPerformanceService = quizPerformanceService;
    this.userReferralService = userReferralService;
    this.browserStatsRepository = browserStatsRepository;
  }
  
  public List<BrowserStats> list(){
    return browserStatsRepository.list();
  }

  public void updateStatistics(String browser) {
    Browser b = Browser.valueOf(browser);
    Result res = userReferralService.getCountByBrowser(b);

    Double userScores = quizPerformanceService.getScoreSumByIds(res.getUsers());
    if (res.getCount() > 0) {
      BrowserStats bs = new BrowserStats(b, res.getCount(), userScores);
      browserStatsRepository.save(bs);
    }
  }
}
